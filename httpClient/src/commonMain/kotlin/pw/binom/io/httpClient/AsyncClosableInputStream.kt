package pw.binom.io.httpClient

import pw.binom.io.AsyncInputStream
import pw.binom.io.EOFException
import pw.binom.io.StreamClosedException
import pw.binom.io.socket.SocketClosedException

class AsyncClosableInputStream(val stream: AsyncInputStream) : AsyncInputStream {
    override suspend fun read(): Byte {
        checkClosed()
        if (read(staticData) != 1)
            throw EOFException()
        return staticData[0]
    }

    private val staticData = ByteArray(1)
    private var eof = false
    private var closed = false
    override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
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