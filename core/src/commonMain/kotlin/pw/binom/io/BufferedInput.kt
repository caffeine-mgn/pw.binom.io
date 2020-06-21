package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.Input

class BufferedInput(val stream: Input, bufferSize: Int = DEFAULT_BUFFER_SIZE) : Input {
    private val buffer = ByteDataBuffer.alloc(bufferSize)
    private var readed = 0
    private var wrote = 0

    val available: Int
        get() {
            if (wrote == 0 || wrote == readed)
                return -1
            return wrote - readed
        }

    override fun skip(length: Long): Long {
        var l = length
        while (l > 0L) {
            if (available == -1) {
                readed = 0
                wrote = stream.read(buffer, 0, buffer.size)
                if (wrote <= 0)
                    wrote
            }
            readed += minOf(available, l.toInt())
        }
        return length
    }

    override fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
        if (available == -1) {
            readed = 0
            wrote = stream.read(buffer, 0, buffer.size)
            if (wrote <= 0)
                return wrote
        }

        val l = minOf(wrote - readed, length)

        buffer.writeTo(readed, data, offset, l)
//        buffer.copyInto(data, destinationOffset = offset, startIndex = readed.value, endIndex = readed.value + l)
        readed += l
        return l
    }

    override fun close() {
        stream.close()
    }
}

fun Input.bufferedInput(bufferSize: Int = DEFAULT_BUFFER_SIZE) = BufferedInput(this, bufferSize)