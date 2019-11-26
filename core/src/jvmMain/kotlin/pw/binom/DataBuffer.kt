package pw.binom

import pw.binom.io.Closeable
import java.nio.ByteBuffer

actual class DataBuffer private constructor(size: Long) : Closeable {
    actual companion object {
        actual fun alloc(size: Long): DataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater that 0")
            return DataBuffer(size)
        }
    }

    private var _buffer: ByteBuffer? = ByteBuffer.allocateDirect(size.toInt())
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
}