package pw.binom.io

import pw.binom.AsyncOutput
import pw.binom.ByteDataBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.copyInto

class AsyncBufferedOutput(val stream: AsyncOutput, bufferSize: Int = DEFAULT_BUFFER_SIZE) : AsyncOutput {
    private val buffer = ByteDataBuffer.alloc(bufferSize)
    private var cursor = 0
    val bufferSize
        get() = buffer.size

    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
        var off = offset
        var len = length
//        var cursor = cursor
        while (len > 0) {
            if (cursor == buffer.size)
                flush()
            val bb = minOf(buffer.size - cursor, len)

            data.copyInto(buffer, cursor, off, off + bb)
            len -= bb
            off += bb
            cursor += bb
        }
//        this.cursor = cursor
        return length - len
    }

    override suspend fun flush() {
        while (cursor > 0) {
            cursor -= stream.write(buffer, 0, cursor)
            stream.flush()
        }
    }

    override suspend fun close() {
        flush()
        stream.close()
    }
}

fun AsyncOutput.bufferedOutput(bufferSize: Int = DEFAULT_BUFFER_SIZE): AsyncBufferedOutput {
    if (this is AsyncBufferedOutput && this.bufferSize == bufferSize)
        return this
    return AsyncBufferedOutput(this, bufferSize)
}