package pw.binom

import pw.binom.io.Closeable

expect class ByteDataBuffer : Closeable, Iterable<Byte> {
    companion object {
        fun alloc(size: Int): ByteDataBuffer
    }

    val size: Int
    operator fun set(index: Int, value: Byte)
    operator fun get(index: Int): Byte
    fun write(position: Int, data: ByteArray, offset: Int, length: Int): Int
    fun read(position: Int, data: ByteArray, offset: Int, length: Int): Int
    fun write(position: Int, data: ByteDataBuffer, offset: Int, length: Int): Int
    fun read(position: Int, data: ByteDataBuffer, offset: Int, length: Int): Int

    override fun iterator(): ByteDataBufferIterator
}

fun ByteDataBuffer.Companion.alloc(size: Int, func: (Int) -> Byte): ByteDataBuffer {
    val data = ByteDataBuffer.alloc(size)
    (0 until size).forEach {
        data[it] = func(it)
    }
    return data
}

fun byteDataOf(bytes: List<Byte>) = ByteDataBuffer.alloc(bytes.size) { bytes[it] }

fun byteDataOf(vararg bytes: Byte): ByteDataBuffer {
    val data = ByteDataBuffer.alloc(bytes.size)
    bytes.forEachIndexed { index, fl ->
        data[index] = fl
    }
    return data
}

class ByteDataBufferIterator(val buffer: ByteDataBuffer) : Iterator<Byte> {
    var cursor = 0
    override fun hasNext(): Boolean = cursor < buffer.size

    override fun next(): Byte {
        if (!hasNext())
            throw NoSuchElementException()
        return buffer[cursor++]
    }

    fun put(value: Byte) {
        if (!hasNext())
            throw NoSuchElementException()
        buffer[cursor++] = value
    }
}

internal inline fun ByteDataBuffer.checkBounds(position: Int, off: Int, len: Int, size: Int) {
    require(off >= 0 && len >= 0)
    if (off + len > size || position + len > size)
        throw IndexOutOfBoundsException()
}