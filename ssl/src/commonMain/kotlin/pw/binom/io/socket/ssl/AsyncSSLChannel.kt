package pw.binom.io.socket.ssl

import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncInputStream
import pw.binom.io.AsyncOutputStream
import pw.binom.io.EOFException

fun SSLSession.asyncChannel(channel: AsyncChannel) =
        AsyncSSLChannel(this, channel)

class AsyncSSLChannel(val session: SSLSession, val channel: AsyncChannel) : AsyncChannel {
    private val buf = ByteBuffer.alloc(1024)

    private suspend fun sendAll() {
        while (true) {
            buf.clear()
            val n = session.readNet(buf)
            if (n == 0)
                break
            buf.flip()
            channel.write(buf)
        }
    }

    private suspend fun readAll() {
        buf.clear()
        channel.read(buf)
        buf.flip()
        session.writeNet(buf)
    }

    override suspend fun close() {
        channel.close()
    }

    override suspend fun write(data: ByteBuffer): Int {
        var len = data.remaining
        val length = data.remaining
        var readed = 0
        LOOP@ while (len > 0) {
            val s = session.writeApp(data)
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

//    override suspend fun write(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        var off = offset
//        var len = length
//        var readed = 0
//        LOOP@ while (len > 0) {
//            val s = session.writeApp(data, off, len)
//            off += s.bytes
//            len -= s.bytes
//            readed += s.bytes
//            sendAll()
//            when (s.state) {
//                SSLSession.State.WANT_WRITE -> {
//                    sendAll()
//                }
//                SSLSession.State.WANT_READ -> {
//                    readAll()
//                }
//                SSLSession.State.OK -> break@LOOP
//                else -> TODO("Unknown state ${s.state}")
//            }
//        }
//        return length - len
//    }

    override suspend fun flush() {
    }

    override suspend fun skip(length: Long): Long {
        TODO("Not yet implemented")
    }

//    override suspend fun read(data: ByteDataBuffer, offset: Int, length: Int): Int {
//        sendAll()
//        var off = offset
//        var len = length
//        var readed = 0
//        LOOP@ while (len > 0) {
//            val s = session.readApp(data, off, len)
//            off += s.bytes
//            len -= s.bytes
//            readed += s.bytes
//            when (s.state) {
//                SSLSession.State.WANT_WRITE -> {
//                    sendAll()
//                }
//                SSLSession.State.WANT_READ -> {
//                    readAll()
//                }
//                SSLSession.State.OK -> {
//                    if (readed > 0)
//                        break@LOOP
//                }
//                else -> TODO("Unknown state ${s.state}")
//            }
//        }
//        return readed
//    }

    override suspend fun read(dest: ByteBuffer): Int {
        sendAll()
        var readed = 0
        LOOP@ while (dest.remaining > 0) {
            val s = session.readApp(dest)
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

}