package pw.binom.io.http

import pw.binom.AsyncOutput
import pw.binom.ByteDataBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.StreamClosedException
import pw.binom.io.UTF8

open class AsyncChunkedOutput(
        val stream: AsyncOutput,
        private val autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE
) : AsyncOutput {
    private var closed = false
    private var finished = false
    val buffer = ByteDataBuffer.alloc(autoFlushBuffer)
    private var bufferPos = 0
    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
        checkClosed()
        var l = length
        var o = offset
        while (l > 0) {
            if (bufferPos == buffer.size) {
                flush()
            }
            val r = minOf(l, (buffer.size - bufferPos))
            data.writeTo(o, buffer, bufferPos, r)
            l -= r
            o += r
            bufferPos += r
        }
        return length
    }

    private val tmp = ByteDataBuffer.alloc(50)
    override suspend fun flush() {
        checkClosed()
        if (buffer.size == 0)
            return
        val r = UTF8.unicodeToUtf8((bufferPos).toString(16), tmp, 0)
        tmp[r] = '\r'.toByte()
        tmp[r + 1] = '\n'.toByte()
        stream.write(tmp, 0, r + 2)
        println("Send chunk. ${bufferPos}")
        stream.write(buffer, 0, bufferPos)
        stream.write(tmp, r, 2)
        stream.flush()
        bufferPos = 0
    }

    suspend fun finish() {
        checkClosed()
        if (finished)
            return
        flush()
        tmp[0] = '0'.toByte()
        tmp[1] = '\r'.toByte()
        tmp[2] = '\n'.toByte()
        tmp[3] = '\r'.toByte()
        tmp[4] = '\n'.toByte()
        stream.write(tmp, 0, 5)
        stream.flush()
        finished = true
    }

    override suspend fun close() {
        finish()
        closed = true
        tmp.close()
    }

    protected fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }
}