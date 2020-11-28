package pw.binom

import pw.binom.io.Closeable

actual class CharBuffer private constructor(var chars: CharArray) : CharSequence, Closeable {
    actual companion object {
        actual fun alloc(size: Int): CharBuffer =
                CharBuffer(CharArray(size))

        actual fun wrap(chars: CharArray): CharBuffer =
                CharBuffer(chars.copyOf())
    }

    actual val capacity: Int
        get() = chars.size
    actual val remaining: Int
        get() = limit - position
    actual var position: Int = 0
        set(value) {
            require(position >= 0)
            require(position <= limit)
            field = value
        }
    actual var limit: Int = 0
        set(value) {
            if (value > capacity || value < 0) throw createLimitException(value)
            field = value
            if (position > value)
                position = value
        }
    actual override val length: Int
        get() = chars.size

    actual fun realloc(newSize: Int): CharBuffer {
        val new = CharArray(newSize)
        chars.copyInto(new, 0, minOf(capacity, newSize))
        val result = CharBuffer(new)
        result.limit = limit
        result.position = position
        return result
    }

    actual fun get(): Char =
            chars[nextPutIndex()]

    actual override fun subSequence(startIndex: Int, endIndex: Int): CharBuffer {
        TODO("Not yet implemented")
    }

    actual operator fun set(index: Int, value: Char) {
        chars[index] = value
    }

    actual fun peek(): Char {
        if (position == limit) {
            throw NoSuchElementException()
        }
        return chars[position]
    }

    actual fun put(value: Char) {
        chars[nextPutIndex()] = value
    }

    actual fun reset(position: Int, length: Int): CharBuffer {
        limit = position + length
        this.position = position
        return this
    }

    actual fun clear(): CharBuffer {
        limit = capacity
        position = 0
        return this
    }

    actual fun read(array: CharArray, offset: Int, length: Int): Int {
        val len = minOf(remaining, length)
        chars.copyInto(array, offset, position, position + len)
        return len
    }

    actual fun flip() {
        limit = position
        position = 0
    }

    actual override fun get(index: Int): Char =
            chars[index]

    override fun close() {
        TODO("Not yet implemented")
    }

    private fun nextPutIndex(): Int {
        if (position >= limit) throw IndexOutOfBoundsException()
        return position++
    }

    private fun createLimitException(newLimit: Int): IllegalArgumentException {
        val msg = if (newLimit > capacity) {
            "newLimit > capacity: ($newLimit > $capacity)"
        } else { // assume negative
            require(newLimit < 0) { "newLimit expected to be negative" }
            "newLimit < 0: ($newLimit < 0)"
        }
        return IllegalArgumentException(msg)
    }

}