package pw.binom.io

import java.io.ByteArrayInputStream as JByteArrayInputStream

actual class ByteArrayInputStream actual constructor(data: ByteArray, offset: Int, length: Int) : InputStream {

    init {
        if (data.size - offset < length)
            throw IndexOutOfBoundsException("Range [$offset, $offset + $length) out of bounds for length ${data.size}")
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int = native.read(data, offset, length)

    override fun close() {
        native.close()
    }

    private val native = JByteArrayInputStream(data, offset, length)
}