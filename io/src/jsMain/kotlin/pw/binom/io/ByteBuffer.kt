package pw.binom.io

import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

private fun memcpy(
    dist: Int8Array,
    distOffset: Int,
    src: Int8Array,
    srcOffset: Int,
    srcLength: Int = src.length - srcOffset
) {
    (srcOffset..(srcOffset + srcLength)).forEachIndexed { index, it ->
        dist[index + distOffset] = src[it]
    }
}

private fun memcpy(
    dist: ByteArray,
    distOffset: Int,
    src: Int8Array,
    srcOffset: Int,
    srcLength: Int = src.length - srcOffset
) {
    (srcOffset..(srcOffset + srcLength)).forEachIndexed { index, it ->
        dist[index + distOffset] = src[it]
    }
}

actual class ByteBuffer(override val capacity: Int) : Input, Output, Closeable, Buffer {
    actual companion object {
        actual fun alloc(size: Int): ByteBuffer = ByteBuffer(size)
    }

    override var position: Int = 0
        set(value) {
            require(value >= 0) { "position should be more or equals 0" }
            require(value <= limit) { "position should be less or equals limit" }
            field = value
        }
    override var limit: Int = capacity
        set(value) {
            checkClosed()
            if (value > capacity || value < 0) throw createLimitException(value)
            field = value
            if (position > value) {
                position = value
            }
        }

    override val remaining123: Int
        @get:JsName("getRemaining")
        get() {
            checkClosed()
            return limit - position
        }

    override val elementSizeInBytes: Int
        get() = 1

    private var closed = false

    private var native = Int8Array(capacity)

    private inline fun checkClosed() {
        if (closed) {
            throw ClosedException()
        }
    }

    override fun flip() {
        limit = position
        position = 0
    }

    override fun compact() {
        if (remaining123 > 0) {
            val size = remaining123
            memcpy(native, 0, native, position, size)
            position = size
            limit = capacity
        } else {
            clear()
        }
    }

    override fun clear() {
        limit = capacity
        position = 0
    }

    actual fun realloc(newSize: Int): ByteBuffer {
        val new = alloc(newSize)
        if (newSize > capacity) {
            memcpy(new.native, 0, native, capacity)
            new.position = position
            new.limit = limit
        } else {
            memcpy(new.native, 0, native, newSize)
            new.position = minOf(position, newSize)
            new.limit = minOf(limit, newSize)
        }
        return new
    }

    actual fun skip(length: Long): Long {
        checkClosed()
        require(length > 0) { "Length must be grade than 0" }

        val pos = minOf((position + length).toInt(), limit)
        val len = pos - position
        position = pos
        return len.toLong()
    }

    actual operator fun get(index: Int): Byte {
        checkClosed()
        return native[index]
    }

    actual fun put(value: Byte) {
        native[position++] = value
    }

    actual fun get(dest: ByteArray, offset: Int, length: Int): Int {
        require(dest.size - offset >= length)
        val l = minOf(remaining123, length)
        memcpy(dest, 0, native, position, l)
        return l
    }

    actual fun peek(): Byte =
        native[position]

    actual fun reset(position: Int, length: Int): ByteBuffer {
        this.position = position
        limit = position + length
        return this
    }

    override fun write(data: ByteBuffer): Int {
        val l = minOf(remaining123, data.remaining123)
        memcpy(native, position, data.native, data.position, l)
        data.position += l
        position += l
        return l
    }

    actual fun get(): Byte =
        native[position++]

    actual operator fun set(index: Int, value: Byte) {
        checkClosed()
        native[index] = value
    }

    actual fun toByteArray(): ByteArray {
        val r = ByteArray(remaining123)
        (position until limit).forEach {
            r[it - position] = native[it]
        }
        return r
    }

    actual fun subBuffer(index: Int, length: Int): ByteBuffer {
        val new = alloc(length)
        memcpy(new.native, 0, native, length)
        new.position = minOf(position, length)
        new.limit = minOf(limit, length)
        return new
    }

    actual fun free() {
        val size = remaining123
        if (size > 0) {
            memcpy(native, 0, native, position, size)
            position = 0
            limit = size
        } else {
            position = 0
            limit = 0
        }
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

    override fun read(dest: ByteBuffer): Int {
        val l = minOf(remaining123, dest.remaining123)
        if (l == 0)
            return l
        memcpy(dest.native, dest.position, native, position, l)
        dest.position += l
        position += l
        return l
    }

    actual fun write(data: ByteArray, offset: Int, length: Int): Int {
        if (offset + length > data.size)
            throw IndexOutOfBoundsException()
        val l = minOf(remaining123, length)
        (offset until (offset + l)).forEach {
            native[position++] = data[it]
        }
        return l
    }
}

actual inline fun <T> ByteBuffer.Companion.alloc(size: Int, block: (ByteBuffer) -> T): T {
    val b = alloc(size)
    return try {
        block(b)
    } finally {
        b.close()
    }
}
