package pw.binom.io.socket.ssl

import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncInputStream
import pw.binom.io.AsyncOutputStream
import pw.binom.io.EOFException

fun SSLSession.asyncChannel(channel: AsyncChannel) =
        AsyncSSLChannel(this, channel)

class AsyncSSLChannel(val session: SSLSession, val channel: AsyncChannel) : AsyncChannel {
    private val buf = ByteArray(1024)

    private suspend fun sendAll() {
        while (true) {
            val n = session.readNet(buf, 0, buf.size)
            if (n == 0)
                break
            channel.output.write(buf, 0, n)
        }
    }

    private suspend fun readAll() {
        val n = channel.input.read(buf)
        session.writeNet(buf, 0, n)
    }

    override val input: AsyncInputStream = object : AsyncInputStream {
        override suspend fun read(): Byte {
            val r = read(buf, 0, 1)
            if (r <= 0)
                throw EOFException()
            return buf[0]
        }

        override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
            sendAll()
            var off = offset
            var len = length
            var readed = 0
            LOOP@ while (len > 0) {
                val s = session.readApp(data, off, len)
                off += s.bytes
                len -= s.bytes
                readed += s.bytes
                when (s.state) {
                    SSLSession.State.WANT_WRITE -> {
                        sendAll()
                    }
                    SSLSession.State.WANT_READ -> {
                        readAll()
                    }
                    SSLSession.State.OK -> {
                        if (readed > 0)
                            break@LOOP
                    }
                    else -> TODO("Unknown state ${s.state}")
                }
            }
            return readed
        }

        override suspend fun close() {
        }
    }
    override val output: AsyncOutputStream = object : AsyncOutputStream {
        override suspend fun write(data: Byte): Boolean {
            buf[0] = data
            return write(buf, 0, 1) == 1
        }

        override suspend fun write(data: ByteArray, offset: Int, length: Int): Int {
            var off = offset
            var len = length
            var readed = 0
            LOOP@ while (len > 0) {
                val s = session.writeApp(data, off, len)
                off += s.bytes
                len -= s.bytes
                readed += s.bytes
                sendAll()
                when (s.state) {
                    SSLSession.State.WANT_WRITE -> {
                        sendAll()
                    }
                    SSLSession.State.WANT_READ -> {
                        readAll()
                    }
                    SSLSession.State.OK -> break@LOOP
                    else -> TODO("Unknown state ${s.state}")
                }
            }
            return length - len
        }

        override suspend fun flush() {
        }

        override suspend fun close() {
        }

    }

    override suspend fun close() {
        channel.close()
    }

}