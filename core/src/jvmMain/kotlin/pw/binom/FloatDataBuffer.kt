package pw.binom

import pw.binom.io.Closeable
import java.nio.ByteBuffer

actual class FloatDataBuffer private constructor(size: Int) : Closeable {
    actual companion object {
        actual fun alloc(size: Int): FloatDataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater that 0")
            return FloatDataBuffer(size)
        }
    }

    private var _buffer: ByteBuffer? = ByteBuffer.allocateDirect(size * 4)
    val buffer: ByteBuffer
        get() {
            val bufferVar = _buffer
            check(bufferVar != null) { "DataBuffer already closed" }
            return bufferVar
        }

    override fun close() {
        check(_buffer == null) { "DataBuffer already closed" }
        _buffer = null
    }

    actual val size: Int
        get() = buffer.capacity() / 4

    actual operator fun set(index: Int, value: Float) {
        val intValue = value.toRawBits()
        val ch1 = ((intValue ushr 24) and 0xFF).toByte()
        val ch2 = ((intValue ushr 16) and 0xFF).toByte()
        val ch3 = ((intValue ushr 8) and 0xFF).toByte()
        val ch4 = ((intValue ushr 0) and 0xFF).toByte()
        buffer.put(index * 4 + 0, ch1)
        buffer.put(index * 4 + 1, ch2)
        buffer.put(index * 4 + 2, ch3)
        buffer.put(index * 4 + 3, ch4)
    }

    actual operator fun get(index: Int): Float {
        val ch1 = buffer.get(index * 4 + 0)
        val ch2 = buffer.get(index * 4 + 1)
        val ch3 = buffer.get(index * 4 + 2)
        val ch4 = buffer.get(index * 4 + 3)
        return Float.fromBits(Int.fromBytes(ch1, ch2, ch3, ch4))
    }
}