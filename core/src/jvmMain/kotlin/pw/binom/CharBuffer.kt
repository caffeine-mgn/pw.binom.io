@file:JvmName("CharBufferUtils")

package pw.binom

import pw.binom.io.Closeable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import java.nio.CharBuffer as JCharBuffer

actual class CharBuffer constructor(val native: JCharBuffer) : CharSequence, Closeable {
    actual companion object {
        actual fun alloc(size: Int): CharBuffer =
            CharBuffer(JCharBuffer.allocate(size))

        actual fun wrap(chars: CharArray): CharBuffer =
            CharBuffer(JCharBuffer.wrap(chars))
    }

    actual val capacity: Int
        get() = native.capacity()

    actual val remaining: Int
        get() = native.remaining()

    actual var position: Int
        get() = native.position()
        set(value) {
            native.position(value)
        }

    actual var limit: Int
        get() = native.limit()
        set(value) {
            native.limit(value)
        }
    actual override val length: Int
        get() = capacity

    actual override operator fun get(index: Int): Char =
        native.get(index)

    actual override fun subSequence(startIndex: Int, endIndex: Int): CharBuffer =
        CharBuffer(native.subSequence(startIndex, endIndex))

    override fun close() {
    }

    actual override fun equals(other: Any?): Boolean =
        when (other) {
            null -> false
            is String -> other == toString()
            is CharBuffer -> other.native == native
            else -> false
        }

    actual operator fun set(index: Int, value: Char) {
        native.put(index, value)
    }

    actual fun peek(): Char {
        if (limit == position) {
            throw NoSuchElementException()
        }
        return native.get(position)
    }

    actual fun get(): Char {
        return native.get()
    }

    actual fun put(value: Char) {
        native.put(value)
    }

    actual fun reset(position: Int, length: Int): CharBuffer {
        this.position = position
        limit = position + length
        return this
    }

    actual fun clear(): CharBuffer {
        native.clear()
        position = 0
        limit = capacity
        return this
    }

    actual override fun toString(): String {
        val p = position
        val result = native.toString()
        position = p
        return result
    }

    actual fun flip() {
        native.flip()
    }

    actual fun read(array: CharArray, offset: Int, length: Int): Int {
        val len = minOf(native.remaining(), length)
        if (len == 0) {
            return 0
        }
        native.get(array, offset, len)
        return len
    }

    actual fun realloc(newSize: Int): CharBuffer {
        val new = alloc(newSize)
        if (newSize > capacity) {
            native.hold(0, capacity) { self ->
                new.native.update(0, native.capacity()) { new ->
                    new.put(self)
                }
            }
            new.position = position
            new.limit = limit
        } else {
            native.hold(0, newSize) { self ->
                new.native.update(0, newSize) { new ->
                    new.put(self)
                }
            }
            new.position = minOf(position, newSize)
            new.limit = minOf(limit, newSize)
        }
        return new
    }

    actual fun subString(startIndex: Int, endIndex: Int): String {
        if (endIndex > capacity) {
            throw ArrayIndexOutOfBoundsException("capacity: [$capacity], startIndex: [$startIndex], endIndex: [$endIndex]")
        }
        val len = minOf(capacity, endIndex - startIndex)
        if (len == 0) {
            return ""
        }
        val array = CharArray(len)
        native.hold(startIndex, len) {
            it.get(array)
        }
        return array.concatToString()
    }
}

@OptIn(ExperimentalContracts::class)
private inline fun <T> JCharBuffer.hold(offset: Int, length: Int, func: (JCharBuffer) -> T): T {
    contract {
        callsInPlace(func)
    }
    val p = position()
    val l = limit()
    try {
        position(offset)
        limit(offset + length)
        return func(this)
    } finally {
        limit(l)
        position(p)
    }
}

@OptIn(ExperimentalContracts::class)
private inline fun <T> JCharBuffer.update(offset: Int, length: Int, func: (JCharBuffer) -> T): T {
    contract {
        callsInPlace(func)
    }
    try {
        position(offset)
        limit(offset + length)
        return func(this)
    } finally {
        clear()
    }
}