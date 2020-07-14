package pw.binom.io.socket.ssl

import pw.binom.ByteDataBuffer
import pw.binom.get
import pw.binom.io.Closeable
import pw.binom.length
import pw.binom.update
import java.nio.ByteBuffer
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult


actual class SSLSession(private val sslEngine: SSLEngine): Closeable {
    actual enum class State {
        OK, WANT_WRITE, WANT_READ, ERROR
    }

    actual class Status(actual val state: State, actual val bytes: Int)

    private var rbio = ByteBuffer.allocateDirect(sslEngine.session.packetBufferSize)
    private var wbio = ByteBuffer.allocateDirect(sslEngine.session.packetBufferSize)
    private val tmpBuf = ByteBuffer.allocateDirect(sslEngine.session.applicationBufferSize)

    init {
        rbio.flip()
//        wbio.flip()
    }

    actual fun readNet(dst: ByteArray, offset: Int, length: Int): Int {
        while (true) {
            if (sslEngine.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
//                val tmpBuf = ByteBuffer.allocateDirect(sslEngine.session.packetBufferSize)
                tmpBuf.clear()
                val s = sslEngine.wrap(tmpBuf, wbio)
                if (s.bytesConsumed() > 0)
                    TODO()
                if (s.status != SSLEngineResult.Status.OK)
                    break
            } else
                break
        }
        wbio.flip()
        val l = minOf(wbio.remaining(), length)
        if (l == 0) {
            wbio.limit(wbio.capacity())
            return 0
        }
        wbio.get(dst, offset, l)
        //wbio.flip()
        wbio.cleanup()
        wbio.compact()
        wbio.limit(wbio.capacity())
        return l
    }

    actual fun writeNet(dst: ByteArray, offset: Int, length: Int): Int {
        val p = rbio.position()
        val l = rbio.limit()
        rbio.position(l)
        rbio.limit(rbio.capacity())
        try {
            rbio.put(dst, offset, length)
        } catch (e: Throwable) {
            throw e
        }
        rbio.limit(l + length)
        rbio.position(p)
        while (true) {
            if (sslEngine.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                tmpBuf.clear()
                val rr = sslEngine.unwrap(rbio, tmpBuf)
                rbio.cleanup()
                if (rr.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    while (true) {
                        sslEngine.delegatedTask?.run() ?: break
                    }
                }
                if (rr.status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                    break
                }
                if (rr.bytesProduced() > 0)
                    TODO()
                continue
            }
            break
        }
        return length
    }

    actual fun writeApp(src: ByteArray, offset: Int, length: Int): Status {
        val s = sslEngine.wrap(ByteBuffer.wrap(src, offset, length), wbio)
        val state = when (s.status) {
            SSLEngineResult.Status.OK ->
                when (s.handshakeStatus) {
                    SSLEngineResult.HandshakeStatus.NEED_UNWRAP -> State.WANT_READ
                    SSLEngineResult.HandshakeStatus.NEED_WRAP -> State.WANT_WRITE
                    SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING,
                    SSLEngineResult.HandshakeStatus.FINISHED -> State.OK
                    SSLEngineResult.HandshakeStatus.NEED_TASK -> {
                        while (true) {
                            sslEngine.delegatedTask?.run() ?: break
                        }
                        State.WANT_WRITE
                    }
                    SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN -> TODO()
                }
            SSLEngineResult.Status.BUFFER_UNDERFLOW -> TODO()
            SSLEngineResult.Status.BUFFER_OVERFLOW -> TODO("wbio size=${wbio.remaining()}")
            SSLEngineResult.Status.CLOSED -> TODO()
        }
        return Status(
                state,
                s.bytesConsumed()
        )
    }

    private val clientData = pw.binom.io.InfinityByteBuffer(512)

    private fun fullBuff(): Status {
        try {
//            val tmpBuf = ByteBuffer.allocateDirect(sslEngine.session.applicationBufferSize)
            tmpBuf.clear()
            val s = sslEngine.unwrap(rbio, tmpBuf)
            tmpBuf.flip()
            clientData.write(pw.binom.ByteBuffer.wrap(tmpBuf))
            rbio.cleanup()
            val state = when (s.status) {
                SSLEngineResult.Status.OK ->
                    when (s.handshakeStatus) {
                        SSLEngineResult.HandshakeStatus.NEED_UNWRAP -> State.WANT_READ
                        SSLEngineResult.HandshakeStatus.NEED_WRAP -> State.WANT_WRITE
                        SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING,
                        SSLEngineResult.HandshakeStatus.FINISHED -> State.OK
                        SSLEngineResult.HandshakeStatus.NEED_TASK -> {
                            while (true) {
                                sslEngine.delegatedTask?.run() ?: break
                            }
                            State.OK
                        }
                        SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN -> TODO()
                    }
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> State.WANT_READ
                SSLEngineResult.Status.BUFFER_OVERFLOW -> TODO()
                SSLEngineResult.Status.CLOSED -> TODO()
            }

            return Status(
                    state,
                    s.bytesProduced()
            )
        } catch (e: Throwable) {
            throw e
        }
    }

//    actual fun readApp(dst: ByteArray, offset: Int, length: Int): Status {
//        while (true) {
//            if (clientData.readRemaining == 0) {
//                val s = fullBuff()
//                if (s.state != State.OK)
//                    return s
//            }
//            val l = minOf(clientData.readRemaining, length)
//            clientData.read(dst, offset, l)
//            return Status(
//                    State.OK,
//                    l
//            )
//        }
//    }

    private fun ByteBuffer.cleanup() {
        if (position() == 0)
            return
        val p = position()
        val l = limit()
        compact()
        position(0)
        limit(l - p)
    }

    actual fun writeApp(src: ByteDataBuffer, offset: Int, length: Int): Status {
        return src.update(offset, length) { buf ->
            val s = sslEngine.wrap(buf, wbio)
            val state = when (s.status) {
                SSLEngineResult.Status.OK ->
                    when (s.handshakeStatus) {
                        SSLEngineResult.HandshakeStatus.NEED_UNWRAP -> State.WANT_READ
                        SSLEngineResult.HandshakeStatus.NEED_WRAP -> State.WANT_WRITE
                        SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING,
                        SSLEngineResult.HandshakeStatus.FINISHED -> State.OK
                        SSLEngineResult.HandshakeStatus.NEED_TASK -> {
                            while (true) {
                                sslEngine.delegatedTask?.run() ?: break
                            }
                            State.WANT_WRITE
                        }
                        SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN -> TODO()
                    }
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> TODO()
                SSLEngineResult.Status.BUFFER_OVERFLOW -> TODO("wbio size=${wbio.remaining()}")
                SSLEngineResult.Status.CLOSED -> TODO()
            }
            Status(
                    state,
                    s.bytesConsumed()
            )
        }
    }

//    actual fun readApp(dst: ByteDataBuffer, offset: Int, length: Int): Status {
//        while (true) {
//            if (clientData.readRemaining == 0) {
//                val s = fullBuff()
//                if (s.state != State.OK)
//                    return s
//            }
//            val l = minOf(clientData.readRemaining, length)
//            clientData.read(dst, offset, l)
//            return Status(
//                    State.OK,
//                    l
//            )
//        }
//    }

    actual fun readNet(dst: ByteDataBuffer, offset: Int, length: Int): Int {
        while (true) {
            if (sslEngine.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
//                val tmpBuf = ByteBuffer.allocateDirect(sslEngine.session.packetBufferSize)
                tmpBuf.clear()
                val s = sslEngine.wrap(tmpBuf, wbio)
                if (s.bytesConsumed() > 0)
                    TODO()
                if (s.status != SSLEngineResult.Status.OK)
                    break
            } else
                break
        }
        wbio.flip()
        val l = minOf(wbio.remaining(), length)
        if (l == 0) {
            wbio.limit(wbio.capacity())
            return 0
        }
        dst.update(offset, l) {
            wbio.get(it)
        }
        //wbio.flip()
        wbio.cleanup()
        wbio.compact()
        wbio.limit(wbio.capacity())
        return l
    }

    actual fun writeNet(dst: ByteDataBuffer, offset: Int, length: Int): Int {
        val p = rbio.position()
        val l = rbio.limit()
        rbio.position(l)
        rbio.limit(rbio.capacity())
        try {
            dst.update(offset, length) {
                rbio.put(it)
            }
        } catch (e: Throwable) {
            throw e
        }
        rbio.limit(l + length)
        rbio.position(p)
        while (true) {
            if (sslEngine.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                tmpBuf.clear()
                val rr = sslEngine.unwrap(rbio, tmpBuf)
                rbio.cleanup()
                if (rr.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    while (true) {
                        sslEngine.delegatedTask?.run() ?: break
                    }
                }
                if (rr.status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                    break
                }
                if (rr.bytesProduced() > 0)
                    TODO()
                continue
            }
            break
        }
        return length
    }

    actual fun readNet(dst: pw.binom.ByteBuffer): Int {
        val length=dst.remaining
        while (true) {
            if (sslEngine.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_WRAP) {
//                val tmpBuf = ByteBuffer.allocateDirect(sslEngine.session.packetBufferSize)
                tmpBuf.clear()
                val s = sslEngine.wrap(tmpBuf, wbio)
                if (s.bytesConsumed() > 0)
                    TODO()
                if (s.status != SSLEngineResult.Status.OK)
                    break
            } else
                break
        }
        wbio.flip()
        val l = minOf(wbio.remaining(), length)
        if (l == 0) {
            wbio.limit(wbio.capacity())
            return 0
        }
        dst.length(l) {
            wbio.get(it.native)
        }
        //wbio.flip()
        wbio.cleanup()
        wbio.compact()
        wbio.limit(wbio.capacity())
        return l
    }

    actual fun writeNet(dst: pw.binom.ByteBuffer): Int {
        val p = rbio.position()
        val l = rbio.limit()
        rbio.position(l)
        rbio.limit(rbio.capacity())
        val l1 = dst.remaining
        try {
            rbio.put(dst.native)
        } catch (e: Throwable) {
            throw e
        }
        rbio.limit(l + l1)
        rbio.position(p)
        while (true) {
            if (sslEngine.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
                tmpBuf.clear()
                val rr = sslEngine.unwrap(rbio, tmpBuf)
                rbio.cleanup()
                if (rr.handshakeStatus == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                    while (true) {
                        sslEngine.delegatedTask?.run() ?: break
                    }
                }
                if (rr.status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                    break
                }
                if (rr.bytesProduced() > 0)
                    TODO()
                continue
            }
            break
        }
        return l1
    }

    actual fun readApp(dst: pw.binom.ByteBuffer): Status {
        while (true) {
            if (clientData.readRemaining == 0) {
                val s = fullBuff()
                if (s.state != State.OK)
                    return s
            }
            val l = minOf(clientData.readRemaining, dst.remaining)
            dst.length(l) { dst ->
                clientData.read(dst)
            }
            return Status(
                    State.OK,
                    l
            )
        }
    }

    actual fun writeApp(src: pw.binom.ByteBuffer): Status {
        val s = sslEngine.wrap(src.native, wbio)
        val state = when (s.status!!) {
            SSLEngineResult.Status.OK ->
                when (s.handshakeStatus!!) {
                    SSLEngineResult.HandshakeStatus.NEED_UNWRAP -> State.WANT_READ
                    SSLEngineResult.HandshakeStatus.NEED_WRAP -> State.WANT_WRITE
                    SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING,
                    SSLEngineResult.HandshakeStatus.FINISHED -> State.OK
                    SSLEngineResult.HandshakeStatus.NEED_TASK -> {
                        while (true) {
                            sslEngine.delegatedTask?.run() ?: break
                        }
                        State.WANT_WRITE
                    }
                    SSLEngineResult.HandshakeStatus.NEED_UNWRAP_AGAIN -> TODO()
                }
            SSLEngineResult.Status.BUFFER_UNDERFLOW -> TODO()
            SSLEngineResult.Status.BUFFER_OVERFLOW -> TODO("wbio size=${wbio.remaining()}")
            SSLEngineResult.Status.CLOSED -> TODO()
        }
        return Status(
                state,
                s.bytesConsumed()
        )
    }

    override fun close() {
        sslEngine.closeOutbound()
//        sslEngine.closeInbound()
    }
}