package pw.binom

import org.khronos.webgl.*
import pw.binom.io.Closeable

actual class IntDataBuffer private constructor(size: Int) : Closeable, Iterable<Int> {
    actual companion object {
        actual fun alloc(size: Int): IntDataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater than 0")
            return IntDataBuffer(size)
        }
    }

    private var _buffer: Int32Array? = Int32Array(size)
    val buffer: Int32Array
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
        get() = buffer.length

    actual operator fun set(index: Int, value: Int) {
        buffer[index] = value
    }

    actual operator fun get(index: Int): Int = buffer[index]

    actual override fun iterator(): IntDataBufferIterator = IntDataBufferIterator(this)
}
