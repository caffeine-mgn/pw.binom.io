package pw.binom

import kotlinx.cinterop.*
import platform.posix.memcpy
import pw.binom.io.Closeable

actual class CharBuffer constructor(val bytes: ByteBuffer) : CharSequence, Closeable, Buffer {
    actual companion object {
        actual fun alloc(size: Int): CharBuffer = CharBuffer(ByteBuffer.alloc(size * Char.SIZE_BYTES))
        actual fun wrap(chars: CharArray): CharBuffer {
            val buf = ByteBuffer.alloc(chars.size * Char.SIZE_BYTES)
            memcpy(buf.refTo(0), chars.refTo(0), (chars.size * Char.SIZE_BYTES).convert())
            return CharBuffer(buf)
        }
    }

    private inline fun div2(value: Int): Int {
        if (value == 0)
            return 0
        return value / 2
    }

    override val capacity: Int
        get() = div2(bytes.capacity)

    override val remaining: Int
        get() = div2(bytes.remaining)

    override var position: Int
        get() = div2(bytes.position)
        set(value) {
            bytes.position = value * 2
        }

    override var limit: Int
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

    override fun refTo(position: Int): CValuesRef<ByteVar> =
        bytes.refTo(position * Char.SIZE_BYTES)

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

    override fun clear() {
        position = 0
        limit = capacity
    }

    override val elementSizeInBytes: Int
        get() = Char.SIZE_BYTES

    actual override fun toString(): String {
        when (remaining) {
            0 -> return ""
            1 -> return get().toString()
        }
        val bb = memScoped {
        refTo(0).getPointer(this).toKString()
        }
        val sb = StringBuilder()
        forEach {
            sb.append(it)
        }
        return sb.toString()
    }

    override fun flip() {
        bytes.flip()
    }

    override fun compact() {
        bytes.compact()
    }

    actual fun read(array: CharArray, offset: Int, length: Int): Int {
        val len = minOf(remaining, length)
        memcpy(array.refTo(offset), bytes.refTo(position * 2), (len * 2).convert())
        position += len
        return len
    }

    actual fun realloc(newSize: Int): CharBuffer =
        CharBuffer(bytes.realloc(newSize * 2))

    actual fun subString(startIndex: Int, endIndex: Int): String {
        if (endIndex > capacity) {
            throw IndexOutOfBoundsException("capacity: [$capacity], startIndex: [$startIndex], endIndex: [$endIndex]")
        }
        val len = minOf(capacity, endIndex - startIndex)
        if (len == 0) {
            return ""
        }
        val array = CharArray(len)
        memcpy(array.refTo(0), bytes.refTo(startIndex * Char.SIZE_BYTES), (len * Char.SIZE_BYTES).convert())
        return array.concatToString()
    }

    actual fun write(array: CharArray, offset: Int, length: Int): Int {
        val len = minOf(remaining, minOf(array.size - offset, length))
        memScoped {
            memcpy(bytes.refTo(position * Char.SIZE_BYTES), array.refTo(offset), (len * Char.SIZE_BYTES).convert())
            for (i in position * Char.SIZE_BYTES until len*Char.SIZE_BYTES){
                println("[$i] -> ${bytes[i]}")
            }
            position += len
        }
        return len
    }
}