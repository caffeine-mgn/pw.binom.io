package pw.binom

import kotlinx.cinterop.*
import platform.posix.free
import platform.posix.malloc
import pw.binom.io.Closeable

actual class IntDataBuffer private constructor(actual val size: Int) : Closeable, Iterable<Int> {
    actual companion object {
        actual fun alloc(size: Int): IntDataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument \"size\" must be greater that 0")
            return IntDataBuffer(size)
        }
    }

    private var _buffer: CPointer<IntVar>? = malloc((size * sizeOf<IntVar>()).convert())!!.reinterpret()
    val buffer: CPointer<IntVar>
        get() {
            val bufferVar = _buffer
            check(bufferVar != null) { "DataBuffer already closed" }
            return bufferVar
        }

    override fun close() {
        check(_buffer != null) { "DataBuffer already closed" }
        free(_buffer)
        _buffer = null
    }

    actual operator fun set(index: Int, value: Int) {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index: $index, Size=$size")
        buffer[index] = value
    }

    actual operator fun get(index: Int): Int {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index: $index, Size=$size")
        return buffer[index]
    }

    actual override fun iterator(): IntDataBufferIterator = IntDataBufferIterator(this)
}