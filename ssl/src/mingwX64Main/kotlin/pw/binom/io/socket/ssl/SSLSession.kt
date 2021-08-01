package pw.binom.io.socket.ssl

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.openssl.*
import pw.binom.ByteBuffer
import pw.binom.ByteDataBuffer
import pw.binom.io.Closeable

internal fun assertError(ssl: CPointer<SSL>, ret: Int) {
    if (ret <= 0 && SSL_get_error(ssl, ret) == SSL_ERROR_SSL) {
        getLastError()
        throw RuntimeException(ERR_error_string(SSL_get_error(ssl, ret).convert(), null)?.toKString())
    }
}

fun getLastError() = ERR_error_string(ERR_peek_last_error(), null)?.toKString()

actual class SSLSession(val ctx: CPointer<SSL_CTX>, val ssl: CPointer<SSL>, val client: Boolean) : Closeable {
    actual enum class State {
        OK, WANT_WRITE, WANT_READ, ERROR, CLOSED
    }

    actual class Status(actual val state: State, actual val bytes: Int) {
        override fun toString(): String {
            return "Status(state: [$state], bytes: [$bytes])"
        }
    }

    private val rbio = BIO_new(BIO_s_mem()!!)!!
    private val wbio = BIO_new(BIO_s_mem()!!)!!

    private fun init(): State? {
        if (SSL_is_init_finished(ssl) != 0)
            return null
        if (client) {
            while (true) {
                val n = SSL_connect(ssl)
                if (n > 0) {
                    break
                }
                assertError(ssl, n)
                val err = SSL_get_error(ssl, n)
                if (err == SSL_ERROR_WANT_WRITE) {
                    return State.WANT_WRITE
                }

                if (err == SSL_ERROR_WANT_READ) {
                    return State.WANT_READ
                }
            }
        } else {
            val n = SSL_accept(ssl)
            assertError(ssl, n)
            return when (SSL_get_error(ssl, n)) {
                SSL_ERROR_WANT_READ -> State.WANT_READ
                SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
                SSL_ERROR_SSL -> State.ERROR
                else -> null
            }
        }
        return null
    }

    init {
        if (client) {
            SSL_set_connect_state(ssl)
        } else {
            SSL_set_accept_state(ssl)
        }
        SSL_ctrl((ssl), SSL_CTRL_MODE, SSL_MODE_AUTO_RETRY.convert(), null)
        SSL_set_bio(ssl, rbio, wbio)
    }

    actual fun readNet(dst: ByteArray, offset: Int, length: Int): Int {
        val n = BIO_read(wbio, dst.refTo(0), dst.size)
        if (n < 0)
            return 0
        return n
    }

    actual fun readNet(dst: ByteDataBuffer, offset: Int, length: Int): Int {
        val n = BIO_read(wbio, dst.refTo(0), dst.size)
        if (n < 0)
            return 0
        return n
    }

    actual fun writeNet(dst: ByteArray, offset: Int, length: Int): Int {
        var len = length
        var off = offset
        var readed = 0


        while (len > 0) {
            val n = BIO_write(rbio, dst.refTo(off), len);
            if (n <= 0)
                TODO()
            readed += n

            off += n
            len -= n
        }
        return readed
    }

    actual fun writeNet(dst: ByteDataBuffer, offset: Int, length: Int): Int {
        var len = length
        var off = offset
        var readed = 0

        while (len > 0) {
            val n = BIO_write(rbio, dst.refTo(off), len)
            if (n <= 0)
                TODO()
            readed += n

            off += n
            len -= n
        }
        return readed
    }

    actual fun writeApp(src: ByteArray, offset: Int, length: Int): Status {
        val r = init()
        if (r != null)
            return Status(
                    r,
                    0
            )
        val n = SSL_write(ssl, src.refTo(offset), length)
        if (n > 0) {
            return Status(
                    State.OK,
                    n
            )
        }
        val state = when (val e = SSL_get_error(ssl, n)) {
            SSL_ERROR_WANT_READ -> State.WANT_READ
            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
            SSL_ERROR_SSL -> State.ERROR
            else -> TODO("Unknown status $e")
        }
        return Status(
                state, 0
        )
    }

//    actual fun readApp(dst: ByteArray, offset: Int, length: Int): Status {
//        val r = init()
//        if (r != null)
//            return Status(
//                    r,
//                    0
//            )
//        val n = SSL_read(ssl, dst.refTo(offset), length)
//        if (n > 0) {
//            return Status(
//                    State.OK,
//                    n
//            )
//        }
//        val state = when (val e = SSL_get_error(ssl, n)) {
//            SSL_ERROR_WANT_READ -> State.WANT_READ
//            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
//            SSL_ERROR_SSL -> State.ERROR
//            else -> TODO("Unknown status $e")
//        }
//        return Status(
//                state, 0
//        )
//    }

    actual fun writeApp(src: ByteDataBuffer, offset: Int, length: Int): Status {
        val r = init()
        if (r != null)
            return Status(
                    r,
                    0
            )
        val n = SSL_write(ssl, src.refTo(offset), length)
        if (n > 0) {
            return Status(
                    State.OK,
                    n
            )
        }
        val state = when (val e = SSL_get_error(ssl, n)) {
            SSL_ERROR_WANT_READ -> State.WANT_READ
            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
            SSL_ERROR_SSL -> State.ERROR
            SSL_ERROR_ZERO_RETURN -> State.CLOSED
            else -> TODO("Unknown status $e on write")
        }
        return Status(
                state, 0
        )
    }

//    actual fun readApp(dst: ByteDataBuffer, offset: Int, length: Int): Status {
//        val r = init()
//        if (r != null)
//            return Status(
//                    r,
//                    0
//            )
//        val n = SSL_read(ssl, dst.refTo(offset), length)
//        if (n > 0) {
//            return Status(
//                    State.OK,
//                    n
//            )
//        }
//        val state = when (val e = SSL_get_error(ssl, n)) {
//            SSL_ERROR_WANT_READ -> State.WANT_READ
//            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
//            SSL_ERROR_SSL -> State.ERROR
//            else -> TODO("Unknown status $e")
//        }
//        return Status(
//                state, 0
//        )
//    }

    actual fun readNet(dst: ByteBuffer): Int {
        val n = dst.ref{ dstPtr,remaining ->
            BIO_read(wbio, dstPtr, remaining)
        }
        if (n < 0)
            return 0
        dst.position += n
        return n
    }

    actual fun writeNet(dst: ByteBuffer): Int {
        var len = dst.remaining
        var off = dst.position
        var readed = 0

        while (len > 0) {
            val n = dst.refTo(off) { dstPtr ->
                BIO_write(rbio, dstPtr, len)
            }
            if (n <= 0)
                TODO()
            readed += n

            off += n
            len -= n
        }
        dst.position += readed
        return readed
    }

    actual fun readApp(dst: ByteBuffer): Status {
        val r = init()
        if (r != null)
            return Status(
                r,
                0
            )
        val n = dst.refTo(dst.position) { dstPtr ->
            SSL_read(ssl, dstPtr, dst.remaining)
        }
        if (n > 0) {
            dst.position += n
            return Status(
                State.OK,
                n
            )
        }
        val state = when (val e = SSL_get_error(ssl, n)) {
            SSL_ERROR_WANT_READ -> State.WANT_READ
            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
            SSL_ERROR_SSL -> State.ERROR
            SSL_ERROR_ZERO_RETURN->State.CLOSED
            else -> TODO("Unknown status $e on read")
        }
        return Status(
                state, 0
        )
    }

    actual fun writeApp(src: ByteBuffer): Status {
        val r = init()
        if (r != null)
            return Status(
                r,
                0
            )
        val n = src.ref { srcPtr, remaining ->
            SSL_write(ssl, srcPtr, remaining)
        }
        if (n > 0) {
            src.position += n
            return Status(
                State.OK,
                n
            )
        }
        assertError(ssl, n)
        val state = when (val e = SSL_get_error(ssl, n)) {
            SSL_ERROR_WANT_READ -> State.WANT_READ
            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
            SSL_ERROR_SSL -> State.ERROR
            SSL_ERROR_ZERO_RETURN -> State.CLOSED
            else -> TODO("Unknown status $e on write")
        }
        return Status(
            state, 0
        )
    }

    override fun close() {
        SSL_CTX_free(ctx)
    }
}