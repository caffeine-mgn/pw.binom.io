package pw.binom.io

import kotlin.math.ceil

private fun memcpy(dist: ByteArray, distOffset: Int, src: ByteArray, srcOffset: Int, srcLength: Int = src.size - srcOffset) {
    (srcOffset..(srcOffset + srcLength)).forEachIndexed { index, it ->
        dist[index + distOffset] = src[it]
    }
}

actual class ByteArrayOutputStream actual constructor(capacity: Int, private val capacityFactor: Float) : OutputStream {
    private var buffer = ByteArray(capacity)
    private var writeLen = 0
    private var closed = false
    actual val size: Int
        get() = writeLen

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        if (closed)
            throw StreamClosedException()

        if (length < 0)
            throw IndexOutOfBoundsException("Length can't be less than 0")

        if (length == 0)
            return 0

        val needWrite = length - (buffer.size - writeLen)
        if (needWrite > 0) {
            val newSize = maxOf(
                    ceil(buffer.size * capacityFactor).toInt(),
                    buffer.size + needWrite
            )

            val new = ByteArray(newSize)
            if (writeLen > 0)
                memcpy(new, 0, buffer, 0, writeLen)
            buffer = new
        }
        memcpy(buffer, writeLen, data, offset, length)
        writeLen += length
        return length
    }

    override fun flush() {
        if (closed)
            throw StreamClosedException()
    }

    override fun close() {
        if (closed)
            throw StreamClosedException()
        closed = true
    }

    actual fun toByteArray(): ByteArray {
        if (closed)
            throw StreamClosedException()
        val out = ByteArray(writeLen)
        if (writeLen > 0)
            memcpy(out, 0, buffer, 0, writeLen)
        return out
    }

}