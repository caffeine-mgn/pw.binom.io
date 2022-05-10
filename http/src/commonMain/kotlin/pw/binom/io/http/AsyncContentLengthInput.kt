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
        if (eof)
            return 0
        val r = if ((contentLength - readed < dest.remaining123.toULong())) {
            val oldLimit = dest.limit
            dest.limit = (contentLength - readed).toInt()
            val l = stream.read(dest)
            dest.limit = oldLimit
            l
        } else
            stream.read(dest)
        readed += r.toULong()
        return r
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
        if (closed)
            throw StreamClosedException()
    }
}
