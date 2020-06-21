package pw.binom.io

import kotlin.math.ceil

actual class ByteArrayOutputStream actual constructor(capacity: Int, private val capacityFactor: Float) : OutputStream {

    private var buffer = ByteArray(capacity)
    private var writeLen = 0
    private var closed = false
    actual val size: Int
        get() = writeLen

    private inline fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        checkClosed()

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
            buffer = buffer.copyOf(newSize)
        }
        data.copyInto(buffer, writeLen, offset, offset + length)
        writeLen += length
        return length
    }

    override fun flush() {
        checkClosed()
    }

    override fun close() {
        checkClosed()
        closed = true
    }

    actual fun toByteArray(): ByteArray {
        checkClosed()
        return if (writeLen > 0)
            buffer.copyOf(writeLen)
        else
            ByteArray(0)
    }
}