package pw.binom

import kotlinx.cinterop.COpaquePointer
import platform.posix.malloc
import platform.posix.free
import kotlinx.cinterop.*
import platform.posix.size_t
import kotlinx.cinterop.convert
import pw.binom.io.Closeable

actual class ByteDataBuffer private constructor(actual val size: Int) : Closeable {
    actual companion object {
        actual fun alloc(size: Int): ByteDataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater that 0")
            return ByteDataBuffer(size)
        }
    }

    private var _buffer: CPointer<ByteVar>? = malloc((size).convert())!!.reinterpret()
    val buffer: CPointer<ByteVar>
        get() {
            val bufferVar = _buffer
            check(bufferVar != null) { "DataBuffer already closed" }
            return bufferVar
        }

    override fun close() {
        check(_buffer == null) { "DataBuffer already closed" }
        _buffer = null
    }

    actual operator fun set(index: Int, value: Byte) {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index: $index, Size=$size")
        buffer[index] = value
    }

    actual operator fun get(index: Int): Byte {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index: $index, Size=$size")
        return buffer[index]
    }
}