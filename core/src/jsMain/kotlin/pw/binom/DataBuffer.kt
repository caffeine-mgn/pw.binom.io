package pw.binom

import org.khronos.webgl.Int8Array
import pw.binom.io.Closeable

actual class DataBuffer private constructor(size: Int) : Closeable {
    actual companion object {
        actual fun alloc(size: Int): DataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater than 0")
            return DataBuffer(size)
        }
    }

    private var _buffer:Int8Array? = Int8Array(size)
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
}