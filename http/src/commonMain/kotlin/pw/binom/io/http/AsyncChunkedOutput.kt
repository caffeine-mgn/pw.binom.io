package pw.binom.io.http

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.AsyncOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.StreamClosedException
import pw.binom.io.UTF8

/**
 * Implements Async Http Chunked Transport Output
 *
 * @param stream real output stream
 * @param autoFlushBuffer size of buffer for auto flush data in buffer
 * @param closeStream flag for auto close [stream] when this stream will close
 */
open class AsyncChunkedOutput(
    stream: AsyncOutput,
    private val autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE,
    closeStream: Boolean = false
) : AsyncOutput {
    var stream: AsyncOutput = stream
        protected set
    var closeStream: Boolean = closeStream
        protected set
    protected var closed = false
    protected var finished = false
    protected val buffer = ByteBuffer(autoFlushBuffer)

    private val tmp = ByteBuffer(50)
    override suspend fun write(data: ByteBuffer): Int {
        checkClosed()
        val len = data.remaining
        while (true) {
            if (data.remaining == 0) {
                break
            }
            if (buffer.remaining == 0) {
                buffer.flip()
                sendBuffer()
            }
            buffer.write(data)
        }
        return len
    }

    private suspend fun sendBuffer() {
        tmp.clear()
        UTF8.unicodeToUtf8((buffer.remaining).toString(16), tmp)
        tmp.put(CR)
        tmp.put(LF)
        tmp.flip()
        stream.writeFully(tmp)
        stream.writeFully(buffer)
        tmp.clear()
        tmp.put(CR)
        tmp.put(LF)
        tmp.flip()
        stream.writeFully(tmp)
        stream.flush()
        buffer.clear()
    }

    override suspend fun flush() {
        checkClosed()
        if (buffer.position == 0) {
            return
        }
        buffer.flip()
        sendBuffer()
    }

    private suspend fun finish() {
        checkClosed()
        if (finished) {
            return
        }
        flush()
        tmp.clear()
        tmp.put('0'.code.toByte())
        tmp.put(CR)
        tmp.put(LF)
        tmp.put(CR)
        tmp.put(LF)
        tmp.flip()
        stream.write(tmp)

        stream.flush()
        finished = true
    }

    protected open fun closeInternalBuffers() {
        tmp.close()
        buffer.close()
    }

    override suspend fun asyncClose() {
        checkClosed()
        if (finished) {
            error("Stream already finished")
        }
        try {
            finish()
            closed = true
            if (closeStream) {
                stream.asyncClose()
            }
        } finally {
            closeInternalBuffers()
        }
    }

    protected fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }
}
