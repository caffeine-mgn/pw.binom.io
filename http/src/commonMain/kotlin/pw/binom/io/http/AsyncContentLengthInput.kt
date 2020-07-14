package pw.binom.io.http

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.io.StreamClosedException

open class AsyncContentLengthInput(val stream: AsyncInput, val contentLength: ULong, val autoCloseStream: Boolean = false) : AsyncHttpInput {

    override val isEof: Boolean
        get() = closed || readed >= contentLength

//    override suspend fun skip(length: Long): Long =
//            stream.skip(length)

    private var readed = 0uL
    private var closed = false
    private val staticData = ByteArray(1)

//    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        checkClosed()
//        if (isEof)
//            return 0
//        val r = if ((contentLength - readed < length.toULong())) {
//            stream.read(data, offset, (contentLength - readed).toInt())
//        } else
//            stream.read(data, offset, length)
//        readed += r.toULong()
//        return r
//    }

    override suspend fun read(dest: ByteBuffer): Int {
        checkClosed()
        if (isEof)
            return 0
        val r = if ((contentLength - readed < dest.remaining.toULong())) {
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

    override suspend fun close() {
        checkClosed()
        closed = true
        if (autoCloseStream) {
            stream.close()
        }
    }

    protected fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }
}