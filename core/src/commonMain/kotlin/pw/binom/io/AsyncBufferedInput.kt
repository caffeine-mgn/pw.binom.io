package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.AsyncInput
import pw.binom.atomic.AtomicInt
import pw.binom.copyInto

open class AsyncBufferedInput(val stream: AsyncInput, bufferSize: Int = DEFAULT_BUFFER_SIZE) : AsyncInput {
    private val buffer = ByteDataBuffer.alloc(bufferSize)
    private var readed = AtomicInt(0)
    private var wrote = AtomicInt(0)

    val available: Int
        get() {
            if (wrote.value == 0 || wrote.value == readed.value)
                return -1
            return wrote.value - readed.value
        }

    override suspend fun skip(length: Long): Long {
        val l = minOf((buffer.size - readed.value).toLong(), length)
        readed.value += l.toInt()
        TODO()
    }

    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
        if (available == -1) {
            readed.value = 0
            wrote.value = stream.read(buffer, 0, buffer.size)
            if (wrote.value <= 0)
                return wrote.value
        }

        val l = minOf(wrote.value - readed.value, length)

        buffer.copyInto(data, destinationOffset = offset, startIndex = readed.value, endIndex = readed.value + l)
        readed.addAndGet(l)
        return l
    }

    override suspend fun close() {
        stream.close()
    }
}

fun AsyncInput.bufferedInput(bufferSize: Int = DEFAULT_BUFFER_SIZE) = AsyncBufferedInput(this, bufferSize)