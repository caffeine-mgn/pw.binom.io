package pw.binom

import kotlinx.cinterop.COpaquePointer
import platform.linux.malloc
import pw.binom.io.Closeable

actual class DataBuffer private constructor(size: Long) : Closeable {
    actual companion object {
        actual fun alloc(size: Long): DataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater that 0")
            return DataBuffer(size)
        }
    }

    private var _buffer:COpaquePointer? = malloc(size.toULong())!!
    val buffer: COpaquePointer
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