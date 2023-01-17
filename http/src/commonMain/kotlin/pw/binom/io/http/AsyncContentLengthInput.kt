package pw.binom.io.http

import pw.binom.io.AsyncInput
import pw.binom.io.ByteBuffer
import pw.binom.io.StreamClosedException
import pw.binom.skipAll

open class AsyncContentLengthInput(
    val stream: AsyncInput,
    val contentLength: ULong,
    val closeStream: Boolean = false
) : AsyncHttpInput {

    override val isEof: Boolean
        get() = eof

    private val eof
        get() = closed || readed >= contentLength

    override val available: Int
        get() = minOf(contentLength - readed, Int.MAX_VALUE.toULong()).toInt()

    private var readed = 0uL
    private var closed = false

    override suspend fun read(dest: ByteBuffer): Int {
        checkClosed()
        if (dest.remaining == 0) {
            return 0
        }
        if (eof) {
            return 0
        }
        val rem = dest.remaining
        val read = if ((contentLength - readed < dest.remaining.toULong())) {
            val oldLimit = dest.limit
            val limit = contentLength - readed
            dest.limit = limit.toInt()
            val read = stream.read(dest)
            dest.limit = oldLimit
            read
        } else {
            stream.read(dest)
        }
        readed += read.toULong()
        return read
    }

    override suspend fun asyncClose() {
        checkClosed()
        if (!isEof) {
            skipAll()
        }
        closed = true
        if (closeStream) {
            stream.asyncClose()
        }
    }

    protected fun checkClosed() {
        if (closed) {
            throw StreamClosedException()
        }
    }
}
