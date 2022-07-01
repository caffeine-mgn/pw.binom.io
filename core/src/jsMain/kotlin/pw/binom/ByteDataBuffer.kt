package pw.binom

/*

actual class ByteDataBuffer : Closeable, Iterable<Byte> {
    actual companion object {
        actual fun alloc(size: Int): ByteDataBuffer {
            if (size <= 0)
                throw IllegalArgumentException("Argument size must be greater than 0")
            return ByteDataBuffer(size)
        }

        fun wrap(array: Int8Array): ByteDataBuffer {
            return ByteDataBuffer(array)
        }

        fun wrap(array: Uint8Array) =
            wrap(Int8Array(array.buffer))

        actual fun wrap(data: ByteArray) =
            wrap(Int8Array(data.unsafeCast<Array<Byte>>()))
    }

    private constructor(size: Int) {
        _buffer = Int8Array(size)
    }

    private constructor(array: Int8Array) {
        _buffer = array
    }

    private var _buffer: Int8Array? = null
    val buffer: Int8Array
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
        get() = buffer.length

    actual operator fun set(index: Int, value: Byte) {
        buffer[index] = value
    }

    actual operator fun get(index: Int): Byte = buffer[index]

    actual override fun iterator(): ByteDataBufferIterator = ByteDataBufferIterator(this)
    actual fun write(position: Int, data: ByteArray, offset: Int, length: Int): Int {
        checkBounds(position, offset, length, data.size)
        for (i in 0 until length) {
            buffer[position + i] = data[i + offset]
        }
        return length
    }

    actual fun read(position: Int, data: ByteArray, offset: Int, length: Int): Int {
        checkBounds(position, offset, length, data.size)
        for (i in 0 until length) {
            data[i + offset] = buffer[position + i]
        }
        return length
    }

    actual fun writeTo(position: Int, data: ByteDataBuffer, offset: Int, length: Int): Int {
        checkBounds(position, offset, length, data.size)
        for (i in 0 until length) {
            buffer[position + i] = data.buffer[i + offset]
        }
        return length
    }

    internal actual fun unsafe() {
    }

    internal actual fun safe() {
    }

    actual fun fill(element: Byte, startIndex: Int, endIndex: Int) {
        (startIndex..endIndex).forEach {
            buffer[it] = element
        }
    }
}
*/
