package pw.binom

import org.khronos.webgl.Float32Array
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import pw.binom.io.Closeable

actual class FloatDataBuffer private constructor(size: Int) : Closeable, Iterable<Float> {
    actual companion object {
        actual fun alloc(size: Int): FloatDataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater than 0")
            return FloatDataBuffer(size)
        }
    }

    private var _buffer: Float32Array? = Float32Array(size)
    val buffer: Float32Array
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

    actual operator fun set(index: Int, value: Float) {
        buffer[index] = value
    }

    actual operator fun get(index: Int): Float = buffer[index]

    actual override fun iterator(): FloatDataBufferIterator = FloatDataBufferIterator(this)
}