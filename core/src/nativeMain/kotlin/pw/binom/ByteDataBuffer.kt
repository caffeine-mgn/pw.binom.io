@file:OptIn(UnsafeNumber::class)

package pw.binom

import kotlinx.cinterop.*
import platform.posix.free
import platform.posix.malloc
import platform.posix.memcpy
import platform.posix.memset
import pw.binom.io.Closeable

actual class ByteDataBuffer private constructor(actual val size: Int, var arr: ByteArray?, ptr: CPointer<ByteVar>?) :
    Closeable, Iterable<Byte> {
    actual companion object {
        actual fun alloc(size: Int): ByteDataBuffer {
            require(size > 0) { "Save must be more than 0. Size: [$size]" }
            return ByteDataBuffer(size, null, malloc((size).convert())!!.reinterpret())
        }

        actual fun wrap(data: ByteArray): ByteDataBuffer {
            require(data.isNotEmpty())
            val ptr = memScoped {
                data.refTo(0).getPointer(this)
            }
            return ByteDataBuffer(data.size, null, ptr)
        }
    }

    private var _buffer: CPointer<ByteVar>? = ptr
    val buffer: CPointer<ByteVar>
        get() {
            if (safe)
                checkClosed()
            return _buffer!!
        }

    private inline fun checkClosed() {
        check(_buffer != null) { "DataBuffer already closed" }
    }

    fun refTo(index: Int): CPointer<ByteVar> {
        if (safe) {
            checkClosed()
            if (index >= size)
                throw IndexOutOfBoundsException("Index: $index, Size=$size")
        }
        return (buffer + index)!!
    }

    override fun close() {
        check(_buffer != null) { "DataBuffer already closed" }
        if (arr == null)
            free(_buffer)
        arr = null
        _buffer = null
    }

    actual operator fun set(index: Int, value: Byte) {
        if (safe && (index < 0 || index >= size))
            throw IndexOutOfBoundsException("Index: $index, Size=$size")
        buffer[index] = value
    }

    actual operator fun get(index: Int): Byte {
        if (safe && (index < 0 || index >= size))
            throw IndexOutOfBoundsException("Index: $index, Size=$size")
        return buffer[index]
    }

    actual override fun iterator(): ByteDataBufferIterator = ByteDataBufferIterator(this)
    actual fun write(position: Int, data: ByteArray, offset: Int, length: Int): Int {
        if (safe)
            checkBounds(position, offset, length, data.size)
        memcpy(buffer + position, data.refTo(offset), length.convert())
        return length
    }

    actual fun read(position: Int, data: ByteArray, offset: Int, length: Int): Int {
        if (safe)
            checkBounds(position, offset, length, data.size)
        memcpy(data.refTo(offset), buffer + position, length.convert())
        return length
    }

    actual fun writeTo(position: Int, data: ByteDataBuffer, offset: Int, length: Int): Int {
        if (safe)
            checkBounds(position, offset, length, data.size)
        memcpy(data.buffer + offset, buffer + position, length.convert())
        return length
    }

    private var safe = true

    internal actual fun unsafe() {
        safe = false
    }

    internal actual fun safe() {
        safe = true
    }

    actual fun fill(element: Byte, startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex)
            return
        if (safe) {
            require(startIndex > endIndex && startIndex >= 0 && endIndex > 0 && endIndex < size && startIndex < size)
        }
        memset(buffer, startIndex, (endIndex - startIndex).convert())
    }
}
