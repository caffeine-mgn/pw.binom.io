package pw.binom

import pw.binom.io.Closeable
import java.nio.ByteBuffer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

actual class ByteDataBuffer : Closeable, Iterable<Byte> {
    actual companion object {
        actual fun alloc(size: Int): ByteDataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater that 0")
            return ByteDataBuffer(size)
        }

        fun wrap(buffer: ByteBuffer) = ByteDataBuffer(buffer)
        actual fun wrap(data: ByteArray): ByteDataBuffer = ByteDataBuffer(ByteBuffer.wrap(data))
    }

    private constructor(size: Int) {
        _buffer = ByteBuffer.allocateDirect(size)
    }

    private constructor(buffer: ByteBuffer) {
        this._buffer = buffer
    }

    private var _buffer: ByteBuffer? = null
    val buffer: ByteBuffer
        get() {
            val bufferVar = _buffer
            check(bufferVar != null) { "DataBuffer already closed" }
            return bufferVar
        }

    override fun close() {
        check(_buffer != null) { "DataBuffer already closed" }
        _buffer = null
    }

    actual val size: Int
        get() = buffer.capacity()

    actual operator fun set(index: Int, value: Byte) {
        buffer.put(index, value)
    }

    actual operator fun get(index: Int): Byte = buffer.get(index)

    actual override fun iterator(): ByteDataBufferIterator = ByteDataBufferIterator(this)

    actual fun write(position: Int, data: ByteArray, offset: Int, length: Int): Int {
        checkBounds(position, offset, length, data.size)
        update(position, length) {
            it.put(data, offset, length)
        }
        return length
    }

    actual fun read(position: Int, data: ByteArray, offset: Int, length: Int): Int {
        checkBounds(position, offset, length, data.size)
        update(position, length) {
            it.get(data, offset, length)
        }
        return length
    }

    actual fun writeTo(position: Int, data: ByteDataBuffer, offset: Int, length: Int): Int {
        checkBounds(position, offset, length, data.size)
        update(position, length) { self ->
            data.update(offset, length) { data ->
                data.put(self)
                data.clear()
            }
        }
        return length
    }

    internal actual fun unsafe() {
    }

    internal actual fun safe() {
    }

    actual fun fill(element: Byte, startIndex: Int, endIndex: Int) {
        (startIndex..endIndex).forEach {
            buffer.put(it, element)
        }
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T> ByteDataBuffer.update(offset: Int, length: Int, func: (ByteBuffer) -> T): T {
    contract {
        callsInPlace(func)
    }
    try {
        buffer.position(offset)
        buffer.limit(offset + length)
        return func(buffer)
    } finally {
        buffer.clear()
    }
}

inline fun ByteBuffer.get(buffer: ByteBuffer) {
    buffer.put(this)
}

