package pw.binom.io

import kotlinx.cinterop.*

sealed interface MemAccess : Closeable {
    val capacity: Int

    fun <T> access(func: (CPointer<ByteVar>) -> T): T
    fun <T> access2(func: (COpaquePointer) -> T): T

    class NativeMemory(val ptr: COpaquePointer, override val capacity: Int) : MemAccess {
        private var closed = false

        constructor(size: Int) : this(
            ptr = Memory.alloc(size.convert()),
            capacity = size,
        )

        override fun <T> access(func: (CPointer<ByteVar>) -> T): T {
            if (closed) {
                throw ClosedException()
            }
            return func(ptr.reinterpret())
        }

        override fun <T> access2(func: (COpaquePointer) -> T): T {
            if (closed) {
                throw ClosedException()
            }
            return func(ptr)
        }

        override fun close() {
            if (closed) {
                return
            }
            closed = true
            Memory.free(ptr)
        }
    }

    class HeapMemory(val ptr: CArrayPointer<ByteVar>, override val capacity: Int) : MemAccess {
        constructor(size: Int) : this(ptr = nativeHeap.allocArray<ByteVar>(size), capacity = size)

        override fun <T> access(func: (CPointer<ByteVar>) -> T): T = func(ptr)
        override fun <T> access2(func: (COpaquePointer) -> T): T = func(ptr)

        override fun close() {
            nativeHeap.free(ptr)
        }
    }

    class ArrayMemory(array: ByteArray) : MemAccess {
        override val capacity: Int = array.size
        private val pin = array.pin()
        override fun <T> access(func: (CPointer<ByteVar>) -> T): T = func(pin.addressOf(0))
        override fun <T> access2(func: (COpaquePointer) -> T): T = func(pin.addressOf(0))

        override fun close() {
            pin.unpin()
        }
    }
}

actual open class ByteBuffer(
    val data: MemAccess,
    val onClose: ((ByteBuffer) -> Unit)?
) : Channel, Buffer, ByteBufferProvider {
    actual companion object {
        actual fun alloc(size: Int): ByteBuffer = ByteBuffer(MemAccess.NativeMemory(size), null)
        actual fun alloc(size: Int, onClose: (ByteBuffer) -> Unit): ByteBuffer =
            ByteBuffer(MemAccess.NativeMemory(size), onClose)

        actual fun wrap(array: ByteArray): ByteBuffer = ByteBuffer(MemAccess.ArrayMemory(array), null)
    }

    //    private val native = createNativeByteBuffer(capacity)!!
    override val capacity: Int
        get() = data.capacity

    init {
        ByteBufferMetric.inc(this)
        ByteBufferAllocationCallback.onCreate?.invoke(this)
    }

    //    val bb = nativeHeap.allocArray<ByteVar>(capacity)
    private var closed = false

    private var _position = 0
    private var _limit = capacity

    override val remaining: Int
        get() = _limit - _position

    override var position: Int
        get() {
            return _position
        }
        set(value) {
            require(value >= 0) { "position ($value) should be more or equal 0" }
            require(value <= limit) { "position ($value) should be less or equal limit ($limit)" }
            _position = value
        }

    fun isReferenceAccessAvailable(position: Int = this.position) =
        capacity != 0 && position < capacity

    private inline fun checkClosed() {
        if (closed) {
            throw ClosedException()
        }
    }

//    val bytes = ByteArray(capacity)

//    val native: CPointer<ByteVar> = run {
//        memScoped {
//            bytes.refTo(0).getPointer(this).toLong()
//        }.toCPointer<ByteVar>()!!
//    }

//    override fun refTo(position: Int): CPointer<ByteVar> {
//        checkClosed()
//        return (native + position)!!
//    }

    override

    fun <T> refTo(position: Int, func: (CPointer<ByteVar>) -> T): T? {
        require(position >= 0) { "position ($position) should be more or equals 0" }
        if (capacity == 0 || position == capacity) {
            return null
        }
        if (position > capacity) {
            throw IllegalArgumentException("position ($position) should be less than capacity ($capacity)")
        }
//        try {
        return data.access {
            func((it + position)!!)
        }
//            return data.usePinned {
//                func(it.addressOf(position))
//            }
//        } catch (e: ArrayIndexOutOfBoundsException) {
//            throw IllegalArgumentException("Can't get access to $position address of buffer memory. capacity: $capacity, data.size: ${data.size}")
//        }
    }

    fun <T> ref(func: (CPointer<ByteVar>, Int) -> T) = refTo(position) {
        func(it, remaining)
    }

    fun <T> ref0(func: (CPointer<ByteVar>, Int) -> T) = refTo(0) { ptr ->
        func(ptr, capacity)
    }

    override fun flip() {
        limit = position
        position = 0
    }

    override var limit: Int
        get() {
//            checkClosed()
            return _limit
        }
        set(value) {
//            checkClosed()
            if (value > capacity || value < 0) throw createLimitException(value)
            _limit = value
            if (position > value) {
                position = value
            }
        }

    actual fun skip(length: Long): Long {
        checkClosed()
        require(length > 0) { "Length must be grade than 0" }

        val pos = minOf((position + length).toInt(), limit)
        val len = pos - position
        position = pos
        return len.toLong()
    }

    override fun read(dest: ByteBuffer): Int {
        checkClosed()
        return ref { sourceCPointer, remaining ->
            dest.ref { destCPointer, destRemaining ->
                val len = minOf(destRemaining, remaining)
                sourceCPointer.copy(
                    dest = destCPointer,
                    size = len.convert(),
                )
                position += len
                dest.position += len
                len
            }
        } ?: 0
    }

    override fun write(data: ByteBuffer): Int {
        checkClosed()
        return data.read(this)
    }

    override fun flush() {
        checkClosed()
    }

    override fun close() {
        checkClosed()
        ByteBufferMetric.dec(this)
//        closed = true
        data.close()
        onClose?.invoke(this)
        ByteBufferAllocationCallback.onFree?.invoke(this)
    }

    private fun createLimitException(newLimit: Int): IllegalArgumentException {
        val msg = if (newLimit > capacity) {
            "newLimit > capacity: ($newLimit > $capacity)"
        } else { // assume negative
            require(newLimit < 0) { "newLimit expected to be negative" }
            "newLimit < 0: ($newLimit < 0)"
        }
        return IllegalArgumentException(msg)
    }

    actual operator fun get(index: Int): Byte {
        checkClosed()
        return data.access { it[index] }
    }

    actual operator fun set(index: Int, value: Byte) {
        checkClosed()
        ref0 { array, dataSize ->
            array[index] = value
        }
    }

    actual fun getByte(): Byte {
        checkClosed()
        val p = position
        if (p >= limit) throw IndexOutOfBoundsException()
        position = p + 1
        return data.access { it[p] }
//        return data[p]
    }

    actual fun reset(position: Int, length: Int): ByteBuffer {
        checkClosed()
        this.position = position
        limit = position + length
        return this
    }

    actual fun put(value: Byte) {
        checkClosed()
        if (position >= limit) throw IndexOutOfBoundsException("Position: [$position], limit: [$limit]")
        ref0 { array, dataSize ->
            array[position++] = value
        }
    }

    override fun clear() {
        checkClosed()
        limit = capacity
        position = 0
    }

    override val elementSizeInBytes: Int
        get() = 1

    actual fun realloc(newSize: Int): ByteBuffer {
        if (newSize == 0) {
            return alloc(0)
        }
        checkClosed()
        if (capacity == 0) {
            return alloc(newSize).empty()
        }
        val new = alloc(newSize)
        val len = minOf(capacity, newSize)
        ref0 { oldCPointer, oldDataSize ->
            new.ref0 { newCPointer, newDataSize ->
                oldCPointer.copy(
                    dest = newCPointer,
                    size = len.convert(),
                )
            }
        }
        new.position = minOf(position, new.capacity)
        new.limit = minOf(limit, new.capacity)
        return new
    }

    actual fun toByteArray(): ByteArray = toByteArray(remaining)
    actual fun toByteArray(limit: Int): ByteArray {
        checkClosed()
        val size = minOf(remaining, limit)
        val r = ByteArray(size)
        if (size > 0) {
            ref { ptr, remaining ->
                r.usePinned {
                    ptr.copy(
                        dest = it.addressOf(0),
                        size = size.convert(),
                    )
                }
            }
        }
        return r
    }

    actual fun write(data: ByteArray, offset: Int, length: Int): Int {
        checkClosed()
        require(offset >= 0) { "Offset argument should be more or equals 0. Actual value is $offset" }
        require(length >= 0) { "Length argument should be more or equals 0. Actual value is $length" }
        if (length == 0) {
            return 0
        }
        if (offset + length > data.size) {
            throw IndexOutOfBoundsException()
        }
        val len = minOf(remaining, length)
        if (len == 0) {
            return 0
        }
        data.usePinned { data ->
            ref0 { cPointer, dataSize ->
                data.addressOf(offset).copy(
                    dest = (cPointer + position)!!.reinterpret(),
                    size = len.convert(),
                )
                position += len
            }
        }

        return len
    }

    override fun compact() {
        checkClosed()
        if (remaining > 0) {
            val size = remaining
            ref0 { cPointer, dataSize ->
                (cPointer + position)!!.copy(
                    dest = cPointer,
                    size = size.convert(),
                )
                position = size
                limit = capacity
            }
        } else {
            clear()
        }
    }

    actual fun peek(): Byte {
        checkClosed()
        if (position == limit) {
            throw NoSuchElementException()
        }
        return this[position]
    }

    actual fun subBuffer(index: Int, length: Int): ByteBuffer {
        checkClosed()
        val newBytes = alloc(length)
        ref0 { oldCPointer, oldDataSize ->
            newBytes.ref0 { newCPointer, newDataSize ->
                (oldCPointer + index)!!.copy(
                    dest = newCPointer,
                    size = length.convert(),
                )
            }
        }

        return newBytes
    }

    actual fun read(dest: ByteArray, offset: Int, length: Int): Int {
        checkClosed()
        require(dest.size - offset >= length) { "length more then available space" }
        return ref0 { cPointer, dataSize ->
            val l = minOf(remaining, length)
            dest.usePinned { dest ->
                (cPointer + position)!!.copy(
                    dest = dest.addressOf(offset),
                    size = l.convert(),
                )
            }
            l
        } ?: 0
    }

    actual fun free() {
        checkClosed()
        val size = remaining
        if (size > 0) {
            ref0 { native, _ ->
                (native + position)!!.copy(
                    dest = native,
                    size = size.convert(),
                )
            }
            position = 0
            limit = size
        } else {
            position = 0
            limit = 0
        }
    }

    override fun get(): ByteBuffer = this

    override fun reestablish(buffer: ByteBuffer) {
        require(buffer === this) { "Buffer should equals this buffer" }
        check(!closed) { "Buffer closed" }
    }
}

private operator fun <T : CPointed> CPointer<T>.plus(offset: Long) =
    (this.toLong() + offset).toCPointer<T>()

private operator fun <T : CPointed> CPointer<T>.plus(offset: Int) =
    (this.toLong() + offset).toCPointer<T>()
