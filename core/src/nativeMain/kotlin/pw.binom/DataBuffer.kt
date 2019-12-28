package pw.binom

import kotlinx.cinterop.COpaquePointer
import platform.posix.malloc
import platform.posix.free
import kotlinx.cinterop.*
import platform.posix.size_t
import kotlinx.cinterop.convert
import pw.binom.io.Closeable

actual class DataBuffer private constructor(size: Int) : Closeable {
    actual companion object {
        actual fun alloc(size: Int): DataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater that 0")
            return DataBuffer(size)
        }
    }

    private var _buffer:COpaquePointer? = malloc(size.convert())!!
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