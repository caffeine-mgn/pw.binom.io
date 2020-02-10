package pw.binom

import pw.binom.io.Closeable
import java.nio.ByteBuffer

actual class ByteDataBuffer : Closeable, Iterable<Byte> {
    actual companion object {
        actual fun alloc(size: Int): ByteDataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater that 0")
            return ByteDataBuffer(size)
        }

        fun wrap(buffer: ByteBuffer) = ByteDataBuffer(buffer)
    }

    private constructor(size: Int) {
        _buffer = ByteBuffer.allocateDirect(size)
    }

    private constructor(buffer: ByteBuffer) {
        this._buffer = buffer
    }

    private var _buffer: ByteBuffer? = null
    val buffer: ByteBuffer
        get() {
            val bufferVar = _buffer
            check(bufferVar != null) { "DataBuffer already closed" }
            return bufferVar
        }

    override fun close() {
        check(_buffer != null) { "DataBuffer already closed" }
        _buffer = null
    }

    actual val size: Int
        get() = buffer.capacity()

    actual operator fun set(index: Int, value: Byte) {
        buffer.put(index, value)
    }

    actual operator fun get(index: Int): Byte = buffer.get(index)

    actual override fun iterator(): ByteDataBufferIterator = ByteDataBufferIterator(this)
}