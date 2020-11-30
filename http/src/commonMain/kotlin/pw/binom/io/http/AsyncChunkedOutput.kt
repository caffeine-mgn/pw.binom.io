package pw.binom.io.http

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.DEFAULT_BUFFER_SIZE
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
        val stream: AsyncOutput,
        private val autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE,
        val closeStream: Boolean = false
) : AsyncOutput {
    private var closed = false
    private var finished = false
    val buffer = ByteBuffer.alloc(autoFlushBuffer)
//    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        checkClosed()
//        var l = length
//        var o = offset
//        while (l > 0) {
//            if (bufferPos == buffer.size) {
//                flush()
//            }
//            val r = minOf(l, (buffer.size - bufferPos))
//            data.writeTo(o, buffer, bufferPos, r)
//            l -= r
//            o += r
//            bufferPos += r
//        }
//        return length
//    }

    private val tmp = ByteBuffer.alloc(50)
    override suspend fun write(data: ByteBuffer): Int {
        checkClosed()
        val len = data.remaining
        while (true) {
            if (data.remaining==0)
                break
            if (buffer.remaining==0) {
                buffer.flip()
                sendBuffer()
            }
            buffer.write(data)
        }
        return len
    }

    private suspend fun sendBuffer(){
        tmp.clear()
        UTF8.unicodeToUtf8((buffer.remaining).toString(16), tmp)
        tmp.put(CR)
        tmp.put(LF)
        tmp.flip()
        stream.write(tmp)
        stream.write(buffer)
        tmp.clear()
        tmp.put(CR)
        tmp.put(LF)
        tmp.flip()
//        tmp.reset(1,2)
        val bb = stream.write(tmp)
        stream.flush()
        buffer.clear()
    }

    override suspend fun flush() {
        checkClosed()
        if (buffer.position == 0)
            return
        buffer.flip()
        sendBuffer()
    }

    private suspend fun finish() {
        checkClosed()
        if (finished)
            return
        flush()
        tmp.clear()
        tmp.put('0'.toByte())
        tmp.put(CR)
        tmp.put(LF)
        tmp.put(CR)
        tmp.put(LF)
        tmp.flip()
        stream.write(tmp)

        stream.flush()
        finished = true
    }

    override suspend fun asyncClose() {
        finish()
        closed = true
        tmp.close()
        if (closeStream) {
            stream.asyncClose()
        }
    }

    protected fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }
}