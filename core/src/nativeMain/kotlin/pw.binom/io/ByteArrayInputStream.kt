package pw.binom.io

import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import platform.posix.memcpy

actual class ByteArrayInputStream actual constructor(private val data: ByteArray, offset: Int, private val length: Int) : InputStream {

    private var cursor: Int = offset

    init {
        if (data.size - offset < length)
            throw IndexOutOfBoundsException("Range [$offset, $offset + $length) out of bounds for length ${data.size}")
    }

    override fun read(data: ByteArray, offset: Int, length: Int): Int {
        if (length - offset < length)
            throw IndexOutOfBoundsException("Range [$offset, $offset + $length) out of bounds for length ${data.size}")

        var max = this.data.size - cursor
        if (max <= 0)
            return 0
        max = minOf(max, length)
        memcpy(data.refTo(offset), this.data.refTo(cursor), max.convert())
        cursor += max
        return max
    }

    override fun close() {
        //NOP
    }
}