package pw.binom.io.socket.ssl

import java.nio.ByteBuffer
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult


actual class SSLSession(private val sslEngine: SSLEngine) {
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
                if (rr.status==SSLEngineResult.Status.BUFFER_UNDERFLOW){
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

    private val clientData = pw.binom.io.ByteBuffer(512)

    private fun fullBuff(): Status {
        try {
//            val tmpBuf = ByteBuffer.allocateDirect(sslEngine.session.applicationBufferSize)
            tmpBuf.clear()
            val s = sslEngine.unwrap(rbio, tmpBuf)
            tmpBuf.flip()
            val b = ByteArray(tmpBuf.remaining())
            tmpBuf.get(b)
            clientData.write(b, 0, b.size)
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

    actual fun readApp(dst: ByteArray, offset: Int, length: Int): Status {
        if (clientData.readRemaining == 0) {
            return fullBuff()
        }
        val l = minOf(clientData.readRemaining, length)
        clientData.read(dst, offset, l)
        return Status(
                State.OK,
                l
        )
    }

    private fun ByteBuffer.cleanup() {
        if (position() == 0)
            return
        val p = position()
        val l = limit()
        compact()
        position(0)
        limit(l - p)
    }
}