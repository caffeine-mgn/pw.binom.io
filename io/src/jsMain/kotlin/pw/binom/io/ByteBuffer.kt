package pw.binom.io

import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

private fun memcpy(
    dist: NativeMem,
    distOffset: Int,
    src: NativeMem,
    srcOffset: Int,
    srcLength: Int = src.size - srcOffset
) {
    if (dist is NativeMem.ArrayNativeMem && src is NativeMem.ArrayNativeMem) {
        src.mem.copyInto(
            destination = dist.mem,
            destinationOffset = distOffset,
            startIndex = srcOffset,
            endIndex = srcOffset + srcLength,
        )
        return
    }
    (srcOffset..(srcOffset + srcLength)).forEachIndexed { index, it ->
        dist[index + distOffset] = src[it]
    }
}

private fun memcpy(
    dist: ByteArray,
    distOffset: Int,
    src: NativeMem,
    srcOffset: Int,
    srcLength: Int = src.size - srcOffset
) {
    (srcOffset..(srcOffset + srcLength)).forEachIndexed { index, it ->
        dist[index + distOffset] = src[it]
    }
}

sealed interface NativeMem {
    val size: Int
    operator fun get(index: Int): Byte
    operator fun set(index: Int, value: Byte)

    class ByteNativeMem(val mem: Int8Array) : NativeMem {
        override val size: Int
            get() = mem.length

        override fun get(index: Int): Byte = mem[index]

        override fun set(index: Int, value: Byte) {
            mem[index] = value
        }
    }

    class ArrayNativeMem(val mem: ByteArray) : NativeMem {
        override val size: Int
            get() = mem.size

        override fun get(index: Int): Byte = mem[index]

        override fun set(index: Int, value: Byte) {
            mem[index] = value
        }
    }
}

actual class ByteBuffer(val native: NativeMem, val onClose: ((ByteBuffer) -> Unit)?) :
    Channel,
    Buffer,
    ByteBufferProvider {
    actual companion object {
        actual fun alloc(size: Int): ByteBuffer = ByteBuffer(NativeMem.ByteNativeMem(Int8Array(size)), null)
        actual fun alloc(size: Int, onClose: (ByteBuffer) -> Unit): ByteBuffer =
            ByteBuffer(NativeMem.ByteNativeMem(Int8Array(size)), onClose)

        actual fun wrap(array: ByteArray): ByteBuffer = ByteBuffer(NativeMem.ArrayNativeMem(array), null)
    }

    override val capacity: Int
        get() = native.size

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

    override val remaining
        get(): Int {
            checkClosed()
            return limit - position
        }

    override val elementSizeInBytes: Int
        get() = 1

    private var closed = false

//    private var native = Int8Array(capacity)

    private fun checkClosed() {
        if (closed) {
            throw ClosedException()
        }
    }

    override fun flip() {
        limit = position
        position = 0
    }

    override fun compact() {
        if (remaining > 0) {
            val size = remaining
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

    actual fun read(dest: ByteArray, offset: Int, length: Int): Int {
        require(dest.size - offset >= length)
        val l = minOf(remaining, length)
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
        val l = minOf(remaining, data.remaining)
        memcpy(native, position, data.native, data.position, l)
        data.position += l
        position += l
        return l
    }

    actual fun getByte(): Byte = native[position++]

    actual operator fun set(index: Int, value: Byte) {
        checkClosed()
        native[index] = value
    }

    actual fun toByteArray(): ByteArray = toByteArray(remaining)

    actual fun toByteArray(limit: Int): ByteArray {
        val size = minOf(limit, remaining)
        val r = ByteArray(remaining)
        val endPosition = position + size
        (position until endPosition).forEach {
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
        val size = remaining
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
        val l = minOf(remaining, dest.remaining)
        if (l == 0)
            return l
        memcpy(dest.native, dest.position, native, position, l)
        dest.position += l
        position += l
        return l
    }

    actual fun write(data: ByteArray, offset: Int, length: Int): Int {
        if (offset + length > data.size) throw IndexOutOfBoundsException()
        val l = minOf(remaining, length)
        (offset until (offset + l)).forEach {
            native[position++] = data[it]
        }
        return l
    }

    override fun get(): ByteBuffer = this

    override fun reestablish(buffer: ByteBuffer) {
        require(buffer === this) { "Buffer should equals this buffer" }
        check(!closed) { "Buffer closed" }
    }
}
