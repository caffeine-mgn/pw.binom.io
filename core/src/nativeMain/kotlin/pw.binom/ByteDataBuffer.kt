package pw.binom

import kotlinx.cinterop.*
import platform.posix.free
import platform.posix.malloc
import platform.posix.memcpy
import pw.binom.io.Closeable

actual class ByteDataBuffer private constructor(actual val size: Int) : Closeable, Iterable<Byte> {
    actual companion object {
        actual fun alloc(size: Int): ByteDataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater that 0")
            return ByteDataBuffer(size)
        }
    }

    private var _buffer: CPointer<ByteVar>? = malloc((size).convert())!!.reinterpret()
    val buffer: CPointer<ByteVar>
        get() {
            val bufferVar = _buffer
            check(bufferVar != null) { "DataBuffer already closed" }
            return bufferVar
        }

    fun refTo(index: Int): CPointer<ByteVar> {
        return (buffer + index)!!
    }

    override fun close() {
        check(_buffer != null) { "DataBuffer already closed" }
        free(_buffer)
        _buffer = null
    }

    actual operator fun set(index: Int, value: Byte) {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index: $index, Size=$size")
        buffer[index] = value
    }

    actual operator fun get(index: Int): Byte {
        if (index < 0 || index >= size)
            throw IndexOutOfBoundsException("Index: $index, Size=$size")
        return buffer[index]
    }

    actual override fun iterator(): ByteDataBufferIterator = ByteDataBufferIterator(this)
    actual fun write(position: Int, data: ByteArray, offset: Int, length: Int): Int {
        checkBounds(position, offset, length, data.size)
        memcpy(buffer + position, data.refTo(offset), length.convert())
        return length
    }

    actual fun read(position: Int, data: ByteArray, offset: Int, length: Int): Int {
        checkBounds(position, offset, length, data.size)
        memcpy(data.refTo(offset), buffer + position, length.convert())
        return length
    }

    actual fun write(position: Int, data: ByteDataBuffer, offset: Int, length: Int): Int {
        checkBounds(position, offset, length, data.size)
        memcpy(buffer + position, data.refTo(offset), length.convert())
        return length
    }

    actual fun read(position: Int, data: ByteDataBuffer, offset: Int, length: Int): Int {
        checkBounds(position, offset, length, data.size)
        memcpy(data.refTo(offset), buffer + position, length.convert())
        return length
    }
}