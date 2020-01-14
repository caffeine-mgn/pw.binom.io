package pw.binom

import pw.binom.io.Closeable
import java.nio.ByteBuffer
import java.nio.ByteOrder

actual class IntDataBuffer private constructor(size: Int) : Closeable, Iterable<Int> {
    actual companion object {
        actual fun alloc(size: Int): IntDataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater that 0")
            return IntDataBuffer(size)
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
        check(_buffer != null) { "DataBuffer already closed" }
        _buffer = null
    }

    actual val size: Int
        get() = buffer.capacity() / 4

    actual operator fun set(index: Int, value: Int) {
        val ch1 = ((value ushr 24) and 0xFF).toByte()
        val ch2 = ((value ushr 16) and 0xFF).toByte()
        val ch3 = ((value ushr 8) and 0xFF).toByte()
        val ch4 = ((value ushr 0) and 0xFF).toByte()
        when (ByteOrder.nativeOrder()) {
            ByteOrder.BIG_ENDIAN -> {
                buffer.put(index * 4 + 0, ch1)
                buffer.put(index * 4 + 1, ch2)
                buffer.put(index * 4 + 2, ch3)
                buffer.put(index * 4 + 3, ch4)
            }
            ByteOrder.LITTLE_ENDIAN -> {
                buffer.put(index * 4 + 3, ch1)
                buffer.put(index * 4 + 2, ch2)
                buffer.put(index * 4 + 1, ch3)
                buffer.put(index * 4 + 0, ch4)
            }
        }

    }

    actual operator fun get(index: Int): Int {
        val ch1 = buffer.get(index * 4 + 0)
        val ch2 = buffer.get(index * 4 + 1)
        val ch3 = buffer.get(index * 4 + 2)
        val ch4 = buffer.get(index * 4 + 3)

        return when (ByteOrder.nativeOrder()) {
            ByteOrder.BIG_ENDIAN -> Int.fromBytes(ch1, ch2, ch3, ch4)
            ByteOrder.LITTLE_ENDIAN -> Int.fromBytes(ch4, ch3, ch2, ch1)
            else -> throw IllegalStateException()
        }
    }

    actual override fun iterator(): IntDataBufferIterator = IntDataBufferIterator(this)
}