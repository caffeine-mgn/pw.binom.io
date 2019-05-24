package pw.binom.io

import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import platform.posix.memcpy
import kotlin.math.ceil

actual class ByteArrayOutputStream actual constructor(capacity: Int, private val capacityFactor: Float) : OutputStream {

    private var buffer = ByteArray(capacity)
    private var writeLen = 0
    private var closed = false
    actual val size: Int
        get() = writeLen

    override fun write(data: ByteArray, offset: Int, length: Int): Int {
        if (closed)
            throw IllegalStateException("Stream is closed")

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
                memcpy(new.refTo(0), buffer.refTo(0), writeLen.convert())
            buffer = new
        }
        memcpy(buffer.refTo(writeLen), data.refTo(offset), length.convert())
        writeLen += length
        return length
    }

    override fun flush() {
        if (closed)
            throw IllegalStateException("Stream is closed")
    }

    override fun close() {
        if (closed)
            throw IllegalStateException("Stream is closed")
        closed = true
    }

    actual fun toByteArray(): ByteArray {
        val out = ByteArray(writeLen)
        if (writeLen>0)
            memcpy(out.refTo(0), buffer.refTo(0), writeLen.convert())
        return out
    }
}