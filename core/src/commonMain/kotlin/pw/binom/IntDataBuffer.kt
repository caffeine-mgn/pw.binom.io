package pw.binom

import pw.binom.io.Closeable

expect class IntDataBuffer : Closeable, Iterable<Int> {
    companion object {
        fun alloc(size: Int): IntDataBuffer
    }

    val size: Int
    operator fun set(index: Int, value: Int)
    operator fun get(index: Int): Int

    override fun iterator(): IntDataBufferIterator
}

fun IntDataBuffer.Companion.alloc(size: Int, func: (Int) -> Int): IntDataBuffer {
    val data = IntDataBuffer.alloc(size)
    (0 until size).forEach {
        data[it] = func(it)
    }
    return data
}

fun intDataOf(ints: List<Int>) = IntDataBuffer.alloc(ints.size) { ints[it] }

fun intDataOf(vararg ints: Int): IntDataBuffer {
    val data = IntDataBuffer.alloc(ints.size)
    ints.forEachIndexed { index, fl ->
        data[index] = fl
    }
    return data
}

class IntDataBufferIterator(val buffer: IntDataBuffer) : Iterator<Int> {
    var cursor = 0
    override fun hasNext(): Boolean = cursor < buffer.size

    override fun next(): Int {
        if (!hasNext())
            throw NoSuchElementException()
        return buffer[cursor++]
    }

    fun put(value: Int) {
        if (!hasNext())
            throw NoSuchElementException()
        buffer[cursor++] = value
    }
}
