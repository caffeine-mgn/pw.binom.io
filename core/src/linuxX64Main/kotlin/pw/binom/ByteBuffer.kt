package pw.binom

import kotlinx.cinterop.*
import platform.posix.memcpy
import pw.binom.atomic.AtomicInt
import pw.binom.io.Closeable
import kotlin.native.concurrent.isFrozen

actual class ByteBuffer(actual val capacity: Int) : Input, Output, Closeable {
    actual companion object {
        actual fun alloc(size: Int): ByteBuffer = ByteBuffer(size)
    }

//    private var closed = false

    private inline fun checkClosed() {
//        if (closed)
//            throw StreamClosedException()
    }

    val bytes = ByteArray(capacity)

    val native: CPointer<ByteVar> = run {
        memScoped {
            bytes.refTo(0).getPointer(this).toLong()
        }.toCPointer<ByteVar>()!!
    }

    fun refTo(position: Int) =
        bytes.refTo(position)

    actual fun flip() {
        limit = position
        position = 0
    }

    actual val remaining: Int
        get() {
            checkClosed()
            return limit - position
        }

    private var _pos by AtomicInt(0)

    actual var position: Int
        get() = _pos
        set(value) {
            require(position >= 0)
            require(position <= limit)
            _pos = value
        }

    private var _limit by AtomicInt(capacity)

    actual var limit: Int
        get() = _limit
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
        val l = minOf(remaining, dest.remaining)
        if (l == 0)
            return l
        memcpy(dest.bytes.refTo(dest.position), bytes.refTo(position), l.convert())
        dest.position += l
        position += l
        return l
    }

    override fun write(data: ByteBuffer): Int {
        val len = minOf(remaining, data.remaining)
        memcpy(bytes.refTo(position), data.bytes.refTo(data.position), len.convert())
        position += len
        data.position += len
        return len
    }

    override fun flush() {
    }

    override fun close() {
        checkClosed()
        //free(native)
//        bytes = ByteArray(0)
//        closed = true
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
        return bytes[index]
    }

    actual operator fun set(index: Int, value: Byte) {
        checkClosed()
        bytes[index] = value
    }

    actual fun get(): Byte =
        bytes[nextPutIndex()]

    actual fun reset(position: Int, length: Int): ByteBuffer {
        this.position = position
        limit = position + length
        return this
    }

    private fun nextPutIndex(): Int {
        if (position >= limit) throw IndexOutOfBoundsException()
        return position++
    }

    actual fun put(value: Byte) {
        if (isFrozen) {
            memScoped {
                bytes.refTo(nextPutIndex()).getPointer(this)[0] = value
            }
        } else {
            bytes[nextPutIndex()] = value
        }
    }

    actual fun clear() {
        limit = capacity
        position = 0
    }

    actual fun realloc(newSize: Int): ByteBuffer {
        val new = ByteBuffer.alloc(newSize)
        if (newSize > capacity) {
            memcpy(new.bytes.refTo(0), bytes.refTo(0), capacity.convert())
            new.position = position
            new.limit = limit
        } else {
            memcpy(new.bytes.refTo(0), bytes.refTo(0), newSize.convert())
            new.position = minOf(position, newSize)
            new.limit = minOf(limit, newSize)
        }
        return new
    }

    actual fun toByteArray(): ByteArray {
        val r = ByteArray(remaining)
        if (remaining > 0) {
            memcpy(r.refTo(0), refTo(position), remaining.convert())
        }
        return r
    }

    actual fun write(data: ByteArray, offset: Int, length: Int): Int {
        if (offset + length > data.size)
            throw IndexOutOfBoundsException()
        val l = minOf(remaining, length)
        memcpy(refTo(position), data.refTo(offset), l.convert())
        return l
    }

    actual fun compact() {
        if (remaining > 0) {
            val size = remaining
            memcpy(bytes.refTo(0), bytes.refTo(position), size.convert())
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