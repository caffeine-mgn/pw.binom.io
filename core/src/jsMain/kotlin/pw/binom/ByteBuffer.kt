package pw.binom

import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import pw.binom.io.Closeable
import pw.binom.io.StreamClosedException

private fun memcpy(dist: Int8Array, distOffset: Int, src: Int8Array, srcOffset: Int, srcLength: Int = src.length - srcOffset) {
    (srcOffset..(srcOffset + srcLength)).forEachIndexed { index, it ->
        dist[index + distOffset] = src[it]
    }
}

actual class ByteBuffer(actual val capacity: Int) : Input, Output, Closeable {
    actual companion object {
        actual fun alloc(size: Int): ByteBuffer = ByteBuffer(size)
    }

    private var closed = false

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    private var native = Int8Array(capacity)//: CPointer<ByteVar> = malloc(capacity.convert())!!.reinterpret()

    actual fun flip() {
        limit = position;
        position = 0;
    }

    actual val remaining: Int
        get() {
            checkClosed()
            return limit - position
        }
    actual var position: Int = 0
        set(value) {
            require(position >= 0)
            require(position < limit)
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

//    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        val len = minOf(data.size - offset, remaining, length)
//        if (len == 0)
//            return 0
//        memcpy(data.buffer, offset, native, position, len)
//        position += len
//        return len
//    }

//    override fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        val len = minOf(data.size - offset, limit - position, length)
//        if (len == 0)
//            return 0
//        memcpy(native, position, data.buffer, offset, length)
//        position += len
//        return len
//    }

    override fun write(data: ByteBuffer): Int {
        val l = minOf(remaining, data.remaining)
        memcpy(native, position, data.native, data.position, l)
        data.position += l
        position += l
        return l
    }

    override fun flush() {
    }

    override fun close() {
        checkClosed()
        native = Int8Array(0)
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

    override fun read(dest: ByteBuffer): Int {
        val l = minOf(remaining, dest.remaining)
        if (l == 0)
            return l
        memcpy(dest.native, dest.position, native, position, l)
        dest.position += l
        position += l
        return l
    }

    actual fun get(): Byte =
            native[position++]

    actual fun reset(position: Int, length: Int): ByteBuffer {
        this.position = position
        limit = position + length
        return this
    }

    actual fun put(value: Byte) {
        native[position++] = value
    }

    actual fun clear() {
        limit = capacity
        position = 0
    }

    actual fun realloc(newSize: Int): ByteBuffer {
        val new = ByteBuffer.alloc(newSize)
        if (newSize > capacity) {
            memcpy(new.native,0, native, capacity)
            new.position = position
            new.limit = limit
        } else {
            memcpy(new.native,0, native, newSize)
            new.position = minOf(position, newSize)
            new.limit = minOf(limit, newSize)
        }
        return new
    }
}