package pw.binom

import kotlinx.cinterop.*
import platform.posix.memcpy
import pw.binom.io.Closeable

actual class CharBuffer constructor(val bytes: ByteBuffer) : CharSequence, Closeable, Buffer {
    actual companion object {
        actual fun alloc(size: Int): CharBuffer = CharBuffer(ByteBuffer.alloc(size * Char.SIZE_BYTES))
        actual fun wrap(chars: CharArray): CharBuffer {
            val buf = ByteBuffer.alloc(chars.size * Char.SIZE_BYTES)
            chars.usePinned { pinnedChars ->
                buf.ref { buf, _ ->
                    memcpy(buf, pinnedChars.addressOf(0), (pinnedChars.get().size * Char.SIZE_BYTES).convert())
                }
            }
            return CharBuffer(buf)
        }
    }

    private inline fun div2(value: Int): Int {
        if (value == 0)
            return 0
        return value / 2
    }

    actual override val capacity: Int
        get() = div2(bytes.capacity)

    actual override val remaining: Int
        get() = div2(bytes.remaining)

    actual override var position: Int
        get() = div2(bytes.position)
        set(value) {
            bytes.position = value * 2
        }

    actual override var limit: Int
        get() = div2(bytes.limit)
        set(value) {
            bytes.limit = value * 2
        }
    actual override val length: Int
        get() = capacity

    actual override operator fun get(index: Int): Char {
        val b1 = bytes[index * 2]
        val b2 = bytes[index * 2 + 1]
        return Short.fromBytes(b2, b1).toInt().toChar()
    }

    actual override fun subSequence(startIndex: Int, endIndex: Int): CharBuffer {
        val newBytes = bytes.subBuffer(div2(startIndex), div2(endIndex - startIndex))
        return CharBuffer(newBytes)
    }

    override fun close() {
        bytes.close()
    }

    override fun <T> refTo(position: Int, func: (CPointer<ByteVar>) -> T): T =
        bytes.refTo(position * Char.SIZE_BYTES, func)

    actual operator fun set(index: Int, value: Char) {
        val s = value.code.toShort()
        bytes[index * 2] = s[1]
        bytes[index * 2 + 1] = s[0]
    }

    actual fun peek(): Char {
        if (limit == position) {
            throw NoSuchElementException()
        }

        val b1 = bytes[position * 2]
        val b2 = bytes[position * 2 + 1]
        return Short.fromBytes(b2, b1).toInt().toChar()
    }

    actual fun get(): Char {
        val b1 = bytes.get()
        val b2 = bytes.get()
        return Short.fromBytes(b2, b1).toInt().toChar()
    }

    actual fun put(value: Char) {
        val s = value.code.toShort()
        bytes.put(s[1])
        bytes.put(s[0])
    }

    actual fun reset(position: Int, length: Int): CharBuffer {
        this.position = position
        limit = position + length
        return this
    }

    actual override fun clear() {
        position = 0
        limit = capacity
    }

    actual override val elementSizeInBytes: Int
        get() = Char.SIZE_BYTES

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

    actual override fun flip() {
        bytes.flip()
    }

    actual override fun compact() {
        bytes.compact()
    }

    actual fun read(array: CharArray, offset: Int, length: Int): Int =
        array.usePinned { pinnedArray ->
            bytes.refTo(position * 2) { bytes ->
                val len = minOf(remaining, length)
                memcpy(pinnedArray.addressOf(offset), bytes, (len * 2).convert())
                position += len
                len
            }
        }


    actual fun realloc(newSize: Int): CharBuffer =
        CharBuffer(bytes.realloc(newSize * Char.SIZE_BYTES))

    actual fun subString(startIndex: Int, endIndex: Int): String {
        if (endIndex > capacity) {
            throw IndexOutOfBoundsException("capacity: [$capacity], startIndex: [$startIndex], endIndex: [$endIndex]")
        }
        val len = minOf(capacity, endIndex - startIndex)
        if (len == 0) {
            return ""
        }
        val array = CharArray(len)
        array.usePinned { pinnedArray ->
            bytes.refTo(startIndex * Char.SIZE_BYTES) { bytes ->
                memcpy(pinnedArray.addressOf(0), bytes, (len * Char.SIZE_BYTES).convert())
            }
        }
        return array.concatToString()
    }

    actual fun write(array: CharArray, offset: Int, length: Int): Int {
        val len = minOf(remaining, minOf(array.size - offset, length))
        array.usePinned { pinnedArray ->
            bytes.refTo(position * Char.SIZE_BYTES) { bytes ->
                memcpy(bytes, pinnedArray.addressOf(offset), (len * Char.SIZE_BYTES).convert())
                position += len
            }
        }
        return len
    }
}