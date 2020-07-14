package pw.binom

import kotlinx.cinterop.*
import platform.posix.malloc
import pw.binom.io.Closeable

actual class FloatDataBuffer private constructor(actual val size: Int) : Closeable, Iterable<Float> {
    actual companion object {
        actual fun alloc(size: Int): FloatDataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument \"size\" must be greater that 0")
            return FloatDataBuffer(size)
        }
    }

    private var _buffer: CPointer<FloatVar>? = malloc((size * sizeOf<FloatVar>()).convert())!!.reinterpret()
    val buffer: CPointer<FloatVar>
        get() {
            val bufferVar = _buffer
            check(bufferVar != null) { "DataBuffer already closed" }
            return bufferVar
        }

    override fun close() {
        check(_buffer != null) { "DataBuffer already closed" }
        _buffer = null
    }

    actual operator fun set(index: Int, value: Float) {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index: $index, Size=$size")
        buffer[index] = value
    }

    actual operator fun get(index: Int): Float {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index: $index, Size=$size")
        return buffer[index]
    }

    actual override fun iterator(): FloatDataBufferIterator = FloatDataBufferIterator(this)
}