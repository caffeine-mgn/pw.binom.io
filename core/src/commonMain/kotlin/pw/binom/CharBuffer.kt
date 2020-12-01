package pw.binom

import pw.binom.io.Closeable

/**
 * A part of memory. Also contents current read/write state
 */
expect class CharBuffer : CharSequence, Closeable {
    companion object {
        fun alloc(size: Int): CharBuffer
        fun wrap(chars: CharArray): CharBuffer
    }

    val capacity: Int
    val remaining: Int
    var position: Int
    var limit: Int
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
    fun clear(): CharBuffer
    override fun toString(): String
    fun read(array: CharArray, offset: Int, length: Int): Int
    fun write(array: CharArray, offset: Int, length: Int): Int
    fun flip()
}

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