package pw.binom

import kotlinx.cinterop.*
import platform.posix.memcpy
import pw.binom.io.Closeable
import pw.binom.io.ClosedException

actual class ByteBuffer(
    override val capacity: Int,
) : Input, Output, Closeable, Buffer {
    actual companion object {
        actual fun alloc(size: Int): ByteBuffer = ByteBuffer(size)
    }

    //    private val native = createNativeByteBuffer(capacity)!!
    private val data = ByteArray(capacity)

    //    val bb = nativeHeap.allocArray<ByteVar>(capacity)
    private var closed = false

    private var _position = 0
    private var _limit = capacity

    override val remaining: Int
        get() {
            checkClosed()
            return limit - position
        }

    override var position: Int
        get() {
            checkClosed()
            return _position
        }
        set(value) {
            checkClosed()
            require(value >= 0) { "position should be more or equal 0" }
            require(value <= limit) { "position should be less or equal limit" }
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
        if (capacity == 0 || position == capacity) {
            return null
        }
        try {
            return data.usePinned {
                func(it.addressOf(position))
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            throw IllegalArgumentException("Can't get access to $position address of buffer memory. capacity: $capacity, data.size: ${data.size}")
        }
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
            checkClosed()
            return _limit
        }
        set(value) {
            checkClosed()
            if (value > capacity || value < 0) throw createLimitException(value)
            _limit = value
            if (position > value)
                position = value
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
                memcpy(destCPointer, sourceCPointer, len.convert())
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
        closed = true
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
        return data[index]
    }

    actual operator fun set(index: Int, value: Byte) {
        checkClosed()
        ref0 { array, dataSize ->
            array[index] = value
        }
    }

    actual fun get(): Byte {
        checkClosed()
        if (position >= limit) throw IndexOutOfBoundsException()
        return data[position++]
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
                memcpy(newCPointer, oldCPointer, len.convert())
            }
        }
        new.position = minOf(position, new.capacity)
        new.limit = minOf(limit, new.capacity)
        return new
    }

    actual fun toByteArray(): ByteArray {
        checkClosed()
        val r = ByteArray(remaining)
        if (remaining > 0) {
            ref { ptr, remaining ->
                r.usePinned {
                    memcpy(it.addressOf(0), ptr, remaining.convert())
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
        if (offset + length > data.size)
            throw IndexOutOfBoundsException()
        val len = minOf(remaining, length)
        if (len == 0) {
            return 0
        }
        data.usePinned { data ->
            ref0 { cPointer, dataSize ->
                memcpy(cPointer + position, data.addressOf(offset), len.convert())
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
                memcpy(cPointer, cPointer + position, size.convert())
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
        val newBytes = ByteBuffer.alloc(length)
        ref0 { oldCPointer, oldDataSize ->
            newBytes.ref0 { newCPointer, newDataSize ->
                memcpy(newCPointer, oldCPointer + index, length.convert())
            }
        }

        return newBytes
    }

    actual fun get(dest: ByteArray, offset: Int, length: Int): Int {
        checkClosed()
        require(dest.size - offset >= length) { "length more then available space" }
        return ref0 { cPointer, dataSize ->
            val l = minOf(remaining, length)
            dest.usePinned { dest ->
                memcpy(dest.addressOf(0), cPointer + position, l.convert())
            }
            l
        } ?: 0
    }

    actual fun free() {
        checkClosed()
        val size = remaining
        if (size > 0) {
            ref0 { native, _ ->
                memcpy(native, native + position, size.convert())
            }
            position = 0
            limit = size
        } else {
            position = 0
            limit = 0
        }
    }

    init {
        doFreeze()
    }
}

private operator fun <T : CPointed> CPointer<T>.plus(offset: Long) =
    (this.toLong() + offset).toCPointer<T>()

private operator fun <T : CPointed> CPointer<T>.plus(offset: Int) =
    (this.toLong() + offset).toCPointer<T>()

actual inline fun <T> ByteBuffer.Companion.alloc(size: Int, block: (ByteBuffer) -> T): T {
    val b = ByteBuffer(size)
    return try {
        block(b)
    } finally {
        b.close()
    }
}
