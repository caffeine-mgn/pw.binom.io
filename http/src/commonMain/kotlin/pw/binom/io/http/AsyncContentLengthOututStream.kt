package pw.binom.io.http

import pw.binom.io.AsyncOutputStream
import pw.binom.io.StreamClosedException

open class AsyncContentLengthOututStream(val stream: AsyncOutputStream, val contentLength: ULong) : AsyncOutputStream {
    override suspend fun write(data: Byte): Boolean {
        checkClosed()
        if (wrote >= contentLength)
            throw IllegalStateException("All Content already send")
        if (wrote + 1uL > contentLength)
            throw IllegalStateException("Can't send more than Content Length")
        val r = stream.write(data)
        if (!r)
            return false
        wrote += 1uL
        return true
    }

    override suspend fun write(data: ByteArray, offset: Int, length: Int): Int {
        checkClosed()
        if (wrote >= contentLength)
            throw IllegalStateException("All Content already send")
        if (wrote + length.toULong() > contentLength)
            throw IllegalStateException("Can't send more than Content Length")
        val r = stream.write(data, offset, length)
        wrote += r.toULong()
        return r
    }

    override suspend fun flush() {
    }

    override suspend fun close() {
        checkClosed()
        closed = true
    }

    private var wrote = 0uL
    private var closed = false

    protected fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }
}