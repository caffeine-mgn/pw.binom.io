package pw.binom.io

import kotlinx.cinterop.*
import kotlin.time.ExperimentalTime

sealed interface MemAccess : Closeable {
    val capacity: Int

    val pointer: CArrayPointer<ByteVar>
    fun <T> access(func: (CPointer<ByteVar>) -> T): T
    fun <T> access2(func: (COpaquePointer) -> T): T

    class NativeMemory(val ptr: COpaquePointer, override val capacity: Int) : MemAccess {
        private var closed = false

        constructor(size: Int) : this(
            ptr = Memory.alloc(size.convert()),
            capacity = size,
        )

        override val pointer: CPointer<ByteVar> = ptr.reinterpret()

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

        override val pointer: CPointer<ByteVar> = ptr

        override fun <T> access(func: (CPointer<ByteVar>) -> T): T = func(ptr)
        override fun <T> access2(func: (COpaquePointer) -> T): T = func(ptr)

        override fun close() {
            nativeHeap.free(ptr)
        }
    }

    class ArrayMemory(array: ByteArray) : MemAccess {
        override val capacity: Int = array.size
        private val pin = array.pin()
        override val pointer: CPointer<ByteVar> = pin.addressOf(0)
        override fun <T> access(func: (CPointer<ByteVar>) -> T): T = func(pin.addressOf(0))
        override fun <T> access2(func: (COpaquePointer) -> T): T = func(pin.addressOf(0))

        override fun close() {
            pin.unpin()
        }
    }
}

actual open class ByteBuffer private constructor(
    val data: MemAccess,
) : Channel, Buffer, ByteBufferProvider {
    actual companion object;

    actual constructor(size: Int) : this(MemAccess.HeapMemory(size))
    actual constructor(array: ByteArray) : this(MemAccess.ArrayMemory(array))

//    actual companion object {
//        actual fun alloc(size: Int): AbstractByteBuffer = AbstractByteBuffer(MemAccess.NativeMemory(size), null)
//        actual fun alloc(size: Int, onClose: (AbstractByteBuffer) -> Unit): AbstractByteBuffer =
//            AbstractByteBuffer(MemAccess.NativeMemory(size), onClose)
//
//        actual fun wrap(array: ByteArray): AbstractByteBuffer = AbstractByteBuffer(MemAccess.ArrayMemory(array), null)
//    }

    //    private val native = createNativeByteBuffer(capacity)!!
    override val capacity: Int
        get() = data.capacity

    override val elementSizeInBytes: Int
        get() = 1

    private var closed = false

    private var _position = 0 // = AtomicInt(0)
    private var _limit = data.capacity // = AtomicInt(data.capacity)

    override val remaining: Int
        get() = _limit - _position

    override var position: Int
        get() = _position
        set(value) {
            if (value > limit || value < 0) {
                throw IllegalArgumentException("Position should be in range between 0 and limit. limit: $limit, new position: $value")
            }
            _position = value
        }

    override var limit: Int
        get() {
            return _limit
        }
        set(value) {
            if (value > capacity || value < 0) throw createLimitException(value)
//            println("ByteBuffer@${hashCode()}->limit=${_limit.getValue()}->$value")
            _limit = value
            if (position > value) {
                position = value
            }
        }

    actual open val isClosed: Boolean
        get() = closed

    init {
        ByteBufferMetric.inc(this)
        ByteBufferAllocationCallback.onCreate(this)
    }

    @PublishedApi
    internal actual open fun ensureOpen() {
        if (closed) {
            throw ClosedException()
        }
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

    fun isReferenceAccessAvailable(position: Int = this.position) = capacity != 0 && position < capacity

    override fun <T> refTo(position: Int, func: (CPointer<ByteVar>) -> T): T? {
        ensureOpen()
        require(position >= 0) { "position ($position) should be more or equals 0" }
        if (capacity == 0 || position == capacity) {
            return null
        }
        if (position > capacity) {
            throw IllegalArgumentException("position ($position) should be less than capacity ($capacity)")
        }
        return func((data.pointer + position)!!)
    }

    inline fun <T : Any> refTo2(defaultValue: T, position: Int, func: (ptr: CPointer<ByteVar>) -> T): T {
        ensureOpen()
        require(position >= 0) { "position ($position) should be more or equals 0" }
        if (capacity == 0 || position == capacity) {
            return defaultValue
        }
        if (position > capacity) {
            throw IllegalArgumentException("position ($position) should be less than capacity ($capacity)")
        }
        return func((data.pointer + position)!!)
    }

    inline fun <T : Any> ref(defaultValue: T, func: (ptr: CPointer<ByteVar>, remaining: Int) -> T) =
        refTo2(defaultValue, position) {
            func(it, remaining)
        }

    inline fun <T : Any> ref0(defaultValue: T, func: (ptr: CPointer<ByteVar>, remaining: Int) -> T) =
        refTo2(defaultValue, 0) { ptr ->
            func(ptr, capacity)
        }

    override fun flip() {
        ensureOpen()
        limit = position
        position = 0
    }

    actual fun skip(length: Long): Long {
        ensureOpen()
        require(length > 0) { "Length must be grade than 0" }

        val pos = minOf((position + length).toInt(), limit)
        val len = pos - position
        position = pos
        return len.toLong()
    }

    @OptIn(ExperimentalTime::class)
    actual fun readInto(dest: ByteBuffer): Int {
        ensureOpen()
        val r = run {
            val remaining2 = remaining
            val destRemaining = dest.remaining
            val len = minOf(destRemaining, remaining2)
            if (len <= 0) {
                0
            } else {
                val sourceCPointer = (data.pointer + position)!!
                val destCPointer = (dest.data.pointer + dest.position)!!
                val p = position
                val p2 = dest.position
                try {
                    position += len
                    dest.position += len
                    sourceCPointer.copyInto(
                        dest = destCPointer,
                        size = len.convert(),
                    )
                    len
                } catch (e: Throwable) {
                    position = p
                    dest.position = p2
                    throw e
                }
            }
//            ref { sourceCPointer, remaining2 ->
//                dest.ref { destCPointer, destRemaining ->
//                    val p = position
//                    val p2 = dest.position
//                    val len = minOf(destRemaining, remaining2)
//                    try {
//                        val copyTime = measureTime {
//                            sourceCPointer.copyInto(
//                                dest = destCPointer,
//                                size = len.convert(),
//                            )
//                            position += len
//                            dest.position += len
//                        }
//
//                        println("ByteBuffer::readInto copyTime: $copyTime")
//
//                        len
//                    } catch (e: Throwable) {
//                        position = p
//                        dest.position = p2
//                        throw e
//                    }
//                }
//            } ?: 0
        }
//        println("ByteBuffer::readInto total: ${r.duration}")
        return r
    }

    override fun read(dest: ByteBuffer): Int = readInto(dest)

    @OptIn(ExperimentalTime::class)
    override fun write(data: ByteBuffer): Int = data.readInto(this)

    override fun flush() {
        ensureOpen()
    }

    override fun close() {
        ensureOpen()
        preClose()
        closed = true
        data.close()
        ByteBufferMetric.dec(this)
        ByteBufferAllocationCallback.onFree(this)
    }

    actual operator fun get(index: Int): Byte {
        ensureOpen()
        return data.pointer[index]
//        return data.access { it[index] }
    }

    actual operator fun set(index: Int, value: Byte) {
        ensureOpen()
        ref0(0) { array, dataSize ->
            array[index] = value
        }
    }

    actual fun getByte(): Byte {
        ensureOpen()
        val p = position
        if (p >= limit) throw EOFException()
        position = p + 1
        return data.pointer[p]
//        return data.access { it[p] }
//        return data[p]
    }

    actual fun reset(position: Int, length: Int): ByteBuffer {
        ensureOpen()
        this.position = position
        limit = position + length
        return this
    }

    actual fun put(value: Byte) {
        ensureOpen()
        if (position >= limit) throw IndexOutOfBoundsException("Position: [$position], limit: [$limit]")
        ref0(0) { array, dataSize ->
            array[position++] = value
        }
    }

    override fun clear() {
        ensureOpen()
        limit = capacity
        position = 0
    }

    actual fun realloc(newSize: Int): ByteBuffer {
        ensureOpen()
        if (newSize == 0) {
            return ByteBuffer(0)
        }
        if (capacity == 0) {
            return ByteBuffer(newSize).empty()
        }
        val new = ByteBuffer(newSize)
        val len = minOf(capacity, newSize)
        ref0(0) { oldCPointer, oldDataSize ->
            new.ref0(0) { newCPointer, newDataSize ->
                oldCPointer.copyInto(
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
        ensureOpen()
        val size = minOf(remaining, limit)
        val r = ByteArray(size)
        if (size > 0) {
            ref(0) { ptr, remaining ->
                r.usePinned {
                    ptr.copyInto(
                        dest = it.addressOf(0),
                        size = size.convert(),
                    )
                }
            }
        }
        return r
    }

    actual fun write(data: ByteArray, offset: Int, length: Int): Int {
        ensureOpen()
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
            ref0(0) { cPointer, dataSize ->
                data.addressOf(offset).copyInto(
                    dest = (cPointer + position)!!.reinterpret(),
                    size = len.convert(),
                )
                position += len
            }
        }

        return len
    }

    override fun compact() {
        ensureOpen()
        if (remaining > 0) {
            val size = remaining
            ref0(0) { cPointer, dataSize ->
                (cPointer + position)!!.copyInto(
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
        ensureOpen()
        if (position == limit) {
            throw NoSuchElementException()
        }
        return this[position]
    }

    actual fun subBuffer(index: Int, length: Int): ByteBuffer {
        ensureOpen()
        val newBytes = ByteBuffer(length)
        ref0(0) { oldCPointer, oldDataSize ->
            newBytes.ref0(0) { newCPointer, newDataSize ->
                (oldCPointer + index)!!.copyInto(
                    dest = newCPointer,
                    size = length.convert(),
                )
            }
        }

        return newBytes
    }

    actual fun readInto(dest: ByteArray, offset: Int, length: Int): Int {
        ensureOpen()
        require(dest.size - offset >= length) { "length more then available space" }
        return ref0(0) { cPointer, dataSize ->
            val l = minOf(remaining, length)
            dest.usePinned { dest ->
                (cPointer + position)!!.copyInto(
                    dest = dest.addressOf(offset),
                    size = l.convert(),
                )
            }
            l
        }
    }

    actual fun free() {
        ensureOpen()
        val size = remaining
        if (size > 0) {
            ref0(0) { native, _ ->
                (native + position)!!.copyInto(
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

    override fun get(): ByteBuffer {
        ensureOpen()
        return this
    }

    override fun reestablish(buffer: ByteBuffer) {
        ensureOpen()
        require(buffer === this) { "Buffer should equals this buffer" }
        check(!closed) { "Buffer closed" }
    }

    protected actual open fun preClose() {
        // Do nothing
    }

    override fun skipAll(bufferSize: Int) {
        position = limit
    }

    override fun skipAll(buffer: ByteBuffer) {
        position = limit
    }

    override fun skip(bytes: Long, buffer: ByteBuffer) {
        internalSkip(bytes)
    }

    override fun skip(bytes: Long, bufferSize: Int) {
        internalSkip(bytes)
    }
}

// private operator fun <T : CPointed> CPointer<T>.plus(offset: Long) =
//    (this.toLong() + offset).toCPointer<T>()

// private operator fun <T : CPointed> CPointer<T>.plus(offset: Int) =
//    (this.toLong() + offset).toCPointer<T>()
