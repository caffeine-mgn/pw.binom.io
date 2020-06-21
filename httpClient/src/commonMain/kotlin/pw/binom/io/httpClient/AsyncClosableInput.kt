package pw.binom.io.httpClient

import pw.binom.ByteDataBuffer
import pw.binom.AsyncInput
import pw.binom.io.StreamClosedException
import pw.binom.io.socket.SocketClosedException

class AsyncClosableInput(val stream: AsyncInput) : AsyncInput {

    private var eof = false
    private var closed = false
    override suspend fun skip(length: Long): Long =
            stream.skip(length)

    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
        checkClosed()
        if (eof)
            return 0
        return try {
            stream.read(data, offset, length)
        } catch (e: SocketClosedException) {
            eof = true
            0
        }
    }

    override suspend fun close() {
        checkClosed()
        closed = true
    }

    private fun checkClosed() {
        if (closed)
            throw StreamClosedException()
    }

}