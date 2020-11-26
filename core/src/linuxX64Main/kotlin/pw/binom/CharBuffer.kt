package pw.binom

import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import platform.posix.memcpy
import pw.binom.io.Closeable

actual class CharBuffer constructor(val bytes: ByteBuffer) : CharSequence, Closeable {
    actual companion object {
        actual fun alloc(size: Int): CharBuffer = CharBuffer(ByteBuffer.alloc(size * 2))
        actual fun wrap(chars: CharArray): CharBuffer {
            val buf = ByteBuffer.alloc(chars.size * Char.SIZE_BYTES)
            memcpy(buf.bytes.refTo(0), chars.refTo(0), (chars.size * Char.SIZE_BYTES).convert())
            return CharBuffer(buf)
        }
    }

    private inline fun div2(value: Int): Int {
        if (value == 0)
            return 0
        return value / 2
    }

    actual val capacity: Int
        get() = div2(bytes.capacity)

    actual val remaining: Int
        get() = div2(bytes.remaining)

    actual var position: Int
        get() = div2(bytes.position)
        set(value) {
            bytes.position = value * 2
        }

    actual var limit: Int
        get() = div2(bytes.limit)
        set(value) {
            bytes.limit = value * 2
        }
    actual override val length: Int
        get() = capacity

    actual override operator fun get(index: Int): Char {
        val b1 = bytes[index * 2]
        val b2 = bytes[index * 2 + 1]
        return Short.fromBytes(b2, b1).toChar()
    }

    actual override fun subSequence(startIndex: Int, endIndex: Int): CharBuffer {
        val newBytes = bytes.subBuffer(div2(startIndex), div2(endIndex - startIndex))
        return CharBuffer(newBytes)
    }

    override fun close() {
        bytes.close()
    }

    actual override fun equals(other: Any?): Boolean =
            when (other) {
                null -> false
                is String -> other == toString()
                is CharBuffer -> other.bytes == bytes
                else -> false
            }

    actual operator fun set(index: Int, value: Char) {
        val s = value.toShort()
        bytes[index * 2] = s[1]
        bytes[index * 2 + 1] = s[0]
    }

    actual fun peek(): Char {
        if (limit == position) {
            throw NoSuchElementException()
        }

        val b1 = bytes[position * 2]
        val b2 = bytes[position * 2 + 1]
        return Short.fromBytes(b2, b1).toChar()
    }

    actual fun get(): Char {
        val b1 = bytes.get()
        val b2 = bytes.get()
        return Short.fromBytes(b2, b1).toChar()
    }

    actual fun put(value: Char) {
        val s = value.toShort()
        bytes.put(s[1])
        bytes.put(s[0])
    }

    actual fun reset(position: Int, length: Int): CharBuffer {
        this.position = position
        limit = position + length
        return this
    }

    actual fun clear(): CharBuffer {
        position = 0
        limit = capacity
        return this
    }

    actual override fun toString(): String {
        when (remaining) {
            0 -> return ""
            1 -> return get().toString()
        }
        val sb = StringBuilder()
        forEach {
            sb.append(it)
        }
        return sb.toString()
    }

    actual fun flip() {
        bytes.flip()
    }

    actual fun read(array: CharArray, offset: Int, length: Int): Int {
        val len = minOf(remaining, length)
        memcpy(array.refTo(offset), bytes.refTo(position), (len * 2).convert())
        position += len
        return len
    }

    actual fun realloc(newSize: Int): CharBuffer =
            CharBuffer(bytes.realloc(newSize * 2))
}