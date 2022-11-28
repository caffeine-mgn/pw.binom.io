package pw.binom

import pw.binom.io.Buffer
import pw.binom.io.Closeable

/**
 * Creates CharBuffer from String. Data of string will coped
 * @receiver String for convert
 * @return coped data of [receiver]
 */
fun CharSequence.toCharBuffer(): CharBuffer {
    if (this is CharBuffer) {
        return this
    }
    val out = CharBuffer.alloc(length)
    forEach {
        out.put(it)
    }
    out.clear()
    return out
}

fun CharBuffer.empty(): CharBuffer {
    position = 0
    limit = 0
    return this
}

fun CharArray.toCharBuffer(): CharBuffer =
    CharBuffer.wrap(this)

inline fun CharBuffer.forEachIndexed(func: (index: Int, value: Char) -> Unit) {
    val pos = position
    val lim = limit
    for (it in pos until lim)
        func(it, this[it])
}

inline fun CharBuffer.forEach(func: (Char) -> Unit) {
    val pos = position
    val lim = limit
    for (it in pos until lim)
        func(this[it])
}

/**
 * A part of memory. Also contents current read/write state
 */
expect class CharBuffer : CharSequence, Closeable, Buffer {
    companion object {
        fun alloc(size: Int): CharBuffer
        fun wrap(chars: CharArray): CharBuffer
    }

    override val length: Int

    fun realloc(newSize: Int): CharBuffer

    override operator fun get(index: Int): Char
    override fun subSequence(startIndex: Int, endIndex: Int): CharBuffer
    fun subString(startIndex: Int, endIndex: Int): String
    override fun equals(other: Any?): Boolean
    operator fun set(index: Int, value: Char)
    fun peek(): Char
    fun get(): Char
    fun put(value: Char)
    fun reset(position: Int, length: Int): CharBuffer
    override fun toString(): String
    fun read(array: CharArray, offset: Int = 0, length: Int = array.size - offset): Int
    fun write(array: CharArray, offset: Int = 0, length: Int = array.size - offset): Int
}
