@file:JvmName("ByteDataBufferCommon")

package pw.binom

import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.pool.DefaultPool
import kotlin.jvm.JvmName
import kotlin.random.Random

fun <T> ByteDataBuffer.unsafe(func: (ByteDataBuffer) -> T): T {
    unsafe()
    try {
        return func(this)
    } finally {
        safe()
    }
}

@Deprecated("Use ByteBuffer")
expect class ByteDataBuffer : Closeable, Iterable<Byte> {
    companion object {
        fun alloc(size: Int): ByteDataBuffer
        fun wrap(data: ByteArray): ByteDataBuffer
    }

    internal fun unsafe()
    internal fun safe()
    fun fill(element: Byte, startIndex: Int = 0, endIndex: Int = size - 1)

    val size: Int
    operator fun set(index: Int, value: Byte)
    operator fun get(index: Int): Byte
    fun write(position: Int, data: ByteArray, offset: Int = 0, length: Int = data.size - offset): Int
    fun read(position: Int, data: ByteArray, offset: Int, length: Int): Int

    fun writeTo(position: Int, data: ByteDataBuffer, offset: Int = 0, length: Int = data.size - offset): Int

    override fun iterator(): ByteDataBufferIterator
}

fun ByteDataBuffer.realloc(size: Int): ByteDataBuffer {
    if (this.size == size)
        return this

    val new = ByteDataBuffer.alloc(size)
    copyInto(new, 0, 0, minOf(size, this.size))
    close()
    return new
}

fun ByteDataBuffer.copyInto(
    destination: ByteDataBuffer,
    destinationOffset: Int = 0,
    startIndex: Int = 0,
    endIndex: Int = minOf(size, destination.size - destinationOffset)
) {
    writeTo(startIndex, destination, destinationOffset, endIndex - startIndex)
}

fun ByteDataBuffer.Companion.alloc(size: Int, func: (Int) -> Byte): ByteDataBuffer {
    val data = alloc(size)
    (0 until size).forEach {
        data[it] = func(it)
    }
    return data
}

fun Random.nextBytes(data: ByteDataBuffer) {
    (0 until data.size).forEachIndexed { index, _ ->
        data[index] = nextInt().toByte()
    }
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

internal fun ByteDataBuffer.checkBounds(position: Int, off: Int, len: Int, size: Int) {
    require(off >= 0 && len >= 0)
    if (off + len > size)
        throw IndexOutOfBoundsException()
}

class ByteDataBufferPool(size: Int = DEFAULT_BUFFER_SIZE) :
    DefaultPool<ByteDataBuffer>(10, { ByteDataBuffer.alloc(size) }), Closeable {
    override fun close() {
        pool.indices.forEach {
            val element = pool[it] as? ByteBuffer?
            if (element != null) {
                element.close()
                pool[it] = null
            }
        }
        this.size = 0
    }
}
