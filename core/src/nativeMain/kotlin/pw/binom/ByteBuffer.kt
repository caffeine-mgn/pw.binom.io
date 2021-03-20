package pw.binom

import kotlinx.cinterop.*
import platform.posix.memcpy
import pw.binom.atomic.AtomicBoolean
import pw.binom.internal.core_native.*
import pw.binom.io.Closeable
import pw.binom.io.ClosedException

actual class ByteBuffer(override val capacity: Int) : Input, Output, Closeable, Buffer {
    actual companion object {
        actual fun alloc(size: Int): ByteBuffer = ByteBuffer(size)
    }

    init {
        BYTE_BUFFER_COUNTER.increment()
    }

    private val native = createNativeByteBuffer(capacity)!!

    private var closed by AtomicBoolean(false)

    private inline fun checkClosed() {
        if (closed)
            throw ClosedException()
    }

//    val bytes = ByteArray(capacity)

//    val native: CPointer<ByteVar> = run {
//        memScoped {
//            bytes.refTo(0).getPointer(this).toLong()
//        }.toCPointer<ByteVar>()!!
//    }

    override fun refTo(position: Int) = run {
        nativeByteBufferRefTo(native, position) ?: throw RuntimeException("Can't get pointer to $position")
    }

    override fun flip() {
        limit = position
        position = 0
    }

    override val remaining: Int
        get() {
            checkClosed()
            return limit - position
        }

    override var position: Int
        get() = native.pointed.position
        set(value) {
            require(value >= 0)
            require(value <= limit)
            native.pointed.position = value
        }


    override var limit: Int
        get() = native.pointed.limit
        set(value) {
            checkClosed()
            if (value > capacity || value < 0) throw createLimitException(value)
            native.pointed.limit = value
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

    override fun read(dest: ByteBuffer): Int = nativeByteBuffer_read(native, dest.native)

    override fun write(data: ByteBuffer): Int = data.read(this)

    override fun flush() {
    }

    override fun close() {
        checkClosed()
        destroyNativeByteBuffer(native)
        BYTE_BUFFER_COUNTER.decrement()
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
        return nativeByteBuffer_getByteIndexed(native, index)
    }

    actual operator fun set(index: Int, value: Byte) {
        checkClosed()
        nativeByteBuffer_setByteIndexed(native, index, value)
    }

    actual fun get(): Byte {
        checkClosed()
        if (position >= limit) throw IndexOutOfBoundsException()
        return nativeByteBuffer_getNextByte(native)
    }

    actual fun reset(position: Int, length: Int): ByteBuffer {
        this.position = position
        limit = position + length
        return this
    }

    actual fun put(value: Byte) {
        checkClosed()
        if (position >= limit) throw IndexOutOfBoundsException("Position: [$position], limit: [$limit]")
        nativeByteBuffer_putNextByte(native, value)
    }

    override fun clear() {
        limit = capacity
        position = 0
    }

    override val elementSizeInBytes: Int
        get() = 1

    actual fun realloc(newSize: Int): ByteBuffer {
        val new = ByteBuffer.alloc(newSize)
        nativeByteBuffer_copy(native, new.native)
        return new
    }

    actual fun toByteArray(): ByteArray {
        val r = ByteArray(remaining)
        if (remaining > 0) {
            nativeByteBuffer_copyToPtr(native, r.refTo(0))
        }
        return r
    }

    actual fun write(data: ByteArray, offset: Int, length: Int): Int {
        if (offset + length > data.size)
            throw IndexOutOfBoundsException()
        val l = minOf(remaining, length)
        memcpy(refTo(position), data.refTo(offset), l.convert())
        position += l
        return l
    }

    override fun compact() {
        if (remaining > 0) {
            val size = remaining
            memcpy(native.pointed.data, native.pointed.data + position, size.convert())
            position = size
            limit = capacity
        } else {
            clear()
        }
    }

    init {
        doFreeze()
    }

    actual fun peek(): Byte {
        if (position == limit) {
            throw NoSuchElementException()
        }
        return this[position]
    }

    actual fun subBuffer(index: Int, length: Int): ByteBuffer {
        val newBytes = ByteBuffer.alloc(length)
        memcpy(newBytes.refTo(0), refTo(index), length.convert())
        return newBytes
    }

    actual fun get(dest: ByteArray, offset: Int, length: Int): Int {
        require(dest.size - offset >= length)
        val l = minOf(remaining, length)
        memcpy(dest.refTo(0), refTo(position), l.convert())
        return l
    }
}