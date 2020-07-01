package pw.binom.io.http

import pw.binom.io.AsyncInputStream
import pw.binom.io.EOFException
import pw.binom.io.StreamClosedException

@Deprecated("Use AsyncContentLengthInput")
open class AsyncContentLengthInputStream(val stream: AsyncInputStream, val contentLength: ULong) : AsyncHttpInputStream {
    override suspend fun read(): Byte {
        if (read(staticData) != 1)
            throw EOFException()
        return staticData[0]
    }

    override val isEof: Boolean
        get() = closed || readed >= contentLength

    private var readed = 0uL
    private var closed = false
    private val staticData = ByteArray(1)

    override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
        checkClosed()
        if (isEof)
            return 0
        val r = if ((contentLength - readed < length.toULong())) {
            stream.read(data, offset, (contentLength - readed).toInt())
        } else
            stream.read(data, offset, length)
        readed += r.toULong()
        return r
    }

    override suspend fun close() {
        checkClosed()
        closed = true
    }

    protected fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }
}