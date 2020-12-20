package pw.binom

import pw.binom.io.Closeable

expect class FloatDataBuffer : Closeable, Iterable<Float> {
    companion object {
        fun alloc(size: Int): FloatDataBuffer
    }

    val size: Int
    operator fun set(index: Int, value: Float)
    operator fun get(index: Int): Float

    override fun iterator(): FloatDataBufferIterator
}

fun FloatDataBuffer.Companion.alloc(size: Int, func: (Int) -> Float): FloatDataBuffer {
    val data = FloatDataBuffer.alloc(size)
    (0 until size).forEach {
        data[it] = func(it)
    }
    return data
}

fun floatDataOf(floats: List<Float>) = FloatDataBuffer.alloc(floats.size) { floats[it] }

fun floatDataOf(vararg floats: Float): FloatDataBuffer {
    val data = FloatDataBuffer.alloc(floats.size)
    floats.forEachIndexed { index, fl ->
        data[index] = fl
    }
    return data
}

class FloatDataBufferIterator(val buffer: FloatDataBuffer) : Iterator<Float> {
    var cursor = 0
    override fun hasNext(): Boolean = cursor < buffer.size

    override fun next(): Float {
        if (!hasNext())
            throw NoSuchElementException()
        return buffer[cursor++]
    }

    fun put(value: Float) {
        if (!hasNext())
            throw NoSuchElementException()
        buffer[cursor++] = value
    }
}