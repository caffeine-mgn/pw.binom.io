package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.Input
import pw.binom.copyInto

class ByteArrayInput(val data: ByteDataBuffer, val offset: Int = 0, val length: Int = data.size - offset) : Input {
    private var cursor: Int = offset

    init {
        if (data.size - offset < length)
            throw IndexOutOfBoundsException("Range [$offset, $offset + $length) out of bounds for length ${data.size}")
    }

    override fun skip(length: Long): Long {
        val l = minOf((data.size - cursor).toLong(), length)
        cursor += l.toInt()
        return l
    }

    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
        if (length > data.size - offset)
            throw IndexOutOfBoundsException("Range [$offset, $offset + $length) out of bounds for length ${data.size}")

        var max = this.length + this.offset - cursor
        if (max <= 0)
            return 0
        max = minOf(max, length)
        this.data.copyInto(data, offset, cursor, cursor + max)
        cursor += max
        return max
    }

    override fun close() {
        //NOP
    }
}

fun ByteDataBuffer.toInput(offset: Int = 0, length: Int = size - offset) =
        ByteArrayInput(this, offset, length)