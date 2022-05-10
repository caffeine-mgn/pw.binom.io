@file:JvmName("CharBufferUtils")

package pw.binom

import pw.binom.io.Buffer
import pw.binom.io.Closeable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import java.nio.CharBuffer as JCharBuffer

actual class CharBuffer constructor(val native: JCharBuffer) : CharSequence, Closeable, Buffer {
    actual companion object {
        actual fun alloc(size: Int): CharBuffer =
            CharBuffer(JCharBuffer.allocate(size))

        actual fun wrap(chars: CharArray): CharBuffer =
            CharBuffer(JCharBuffer.wrap(chars))
    }

    override val capacity: Int
        get() = native.capacity()

    override val remaining123: Int
        get() = native.remaining()

    override var position: Int
        get() = native.position()
        set(value) {
            native.position(value)
        }

    override var limit: Int
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
        // Do nothing
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

    override fun clear() {
        native.clear()
        position = 0
        limit = capacity
    }

    override val elementSizeInBytes: Int
        get() = Char.SIZE_BYTES

    actual override fun toString(): String {
        val p = position
        val result = native.toString()
        position = p
        return result
    }

    override fun flip() {
        native.flip()
    }

    override fun compact() {
        native.compact()
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
            throw IndexOutOfBoundsException("capacity: [$capacity], startIndex: [$startIndex], endIndex: [$endIndex]")
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

    actual fun write(array: CharArray, offset: Int, length: Int): Int {
        val len = minOf(remaining123, minOf(array.size - offset, length))
        native.put(array, offset, len)
        return len
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
