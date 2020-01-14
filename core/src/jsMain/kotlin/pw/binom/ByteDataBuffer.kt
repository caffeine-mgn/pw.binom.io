package pw.binom

import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import pw.binom.io.Closeable

actual class ByteDataBuffer private constructor(size: Int) : Closeable {
    actual companion object {
        actual fun alloc(size: Int): ByteDataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater than 0")
            return ByteDataBuffer(size)
        }
    }

    private var _buffer: Int8Array? = Int8Array(size)
    val buffer: Int8Array
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
        get() = buffer.length

    actual operator fun set(index: Int, value: Byte) {
        buffer[index] = value
    }

    actual operator fun get(index: Int): Byte = buffer[index]
}