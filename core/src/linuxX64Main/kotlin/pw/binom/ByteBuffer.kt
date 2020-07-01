package pw.binom

import kotlinx.cinterop.*
import platform.posix.free
import platform.posix.malloc
import platform.posix.memcpy
import pw.binom.io.Closeable
import pw.binom.io.StreamClosedException

actual class ByteBuffer(actual val capacity: Int) : Input, Output, Closeable {
    actual companion object {
        actual fun alloc(size: Int): ByteBuffer = ByteBuffer(size)
    }

    private var closed = false

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    val native: CPointer<ByteVar> = malloc(capacity.convert())!!.reinterpret()

    actual fun flip() {
        limit = position
        position = 0
    }

    actual val remaining: Int
        get() {
            checkClosed()
            return limit - position
        }
    actual var position: Int = 0
        set(value) {
            require(position >= 0)
            require(position <= limit)
            field = value
        }
    actual var limit: Int = capacity
        set(value) {
            checkClosed()
            if (value > capacity || value < 0) throw createLimitException(value)
            field = value
            if (position > value)
                position = value
        }

    override fun skip(length: Long): Long {
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
        memcpy(dest.native + dest.position, native + position, l.convert())
        dest.position += l
        position += l
        return l
    }

    override fun write(data: ByteBuffer): Int {
        val len = minOf(remaining, data.remaining)
        memcpy(native + position, data.native + data.position, len.convert())
        position += len
        data.position += len
        return len
    }

    override fun flush() {
    }

    override fun close() {
        checkClosed()
        free(native)
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
        return native[index]
    }

    actual operator fun set(index: Int, value: Byte) {
        checkClosed()
        native[index] = value
    }

    actual fun get(): Byte =
            native[position++]

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
        native[nextPutIndex()] = value
    }

    actual fun clear() {
        limit = capacity
        position = 0
    }

    actual fun realloc(newSize: Int): ByteBuffer {
        val new = ByteBuffer.alloc(newSize)
        if (newSize > capacity) {
            memcpy(new.native, native, capacity.convert())
            new.position = position
            new.limit = limit
        } else {
            memcpy(new.native, native, newSize.convert())
            new.position = minOf(position, newSize)
            new.limit = minOf(limit, newSize)
        }
        return new
    }
}