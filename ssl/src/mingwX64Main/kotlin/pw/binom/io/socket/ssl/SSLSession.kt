package pw.binom.io.socket.ssl

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.openssl.*
import platform.posix.free

actual class SSLSession(val ssl: CPointer<SSL>, val client: Boolean) {
    actual enum class State {
        OK, WANT_WRITE, WANT_READ, ERROR
    }

    actual class Status(actual val state: State, actual val bytes: Int)

    private val rbio = BIO_new(BIO_s_mem()!!)!!
    private val wbio = BIO_new(BIO_s_mem()!!)!!

    private var inited = false

    private fun init(): State? {
        if (inited)
            return null
        if (client) {
            while (true) {
                println("Try connect")
                val n = SSL_connect(ssl)
                if (n > 0) {
                    println("SSL connected")
                    break
                }
                val err = SSL_get_error(ssl, n)
                if (err == SSL_ERROR_WANT_WRITE) {
                    println("SSL connect need to write")
                    return State.WANT_WRITE
                }

                if (err == SSL_ERROR_WANT_READ) {
                    println("SSL connect need to read")
                    return State.WANT_READ
                }
            }
        } else {
            SSL_accept(ssl)
        }

        inited = true
        return null
    }

    init {
        SSL_set_bio(ssl, rbio, wbio)
    }

    actual fun readNet(dst: ByteArray, offset: Int, length: Int): Int {
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

    actual fun readApp(dst: ByteArray, offset: Int, length: Int): Status {
        val r = init()
        if (r != null)
            return Status(
                    r,
                    0
            )
        val n = SSL_read(ssl, dst.refTo(offset), length)
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
}

private fun CPointer<SSL>.getError(ret: Int): String {
    val r = getSSLError(ret)!!

    val str = memScoped {
        r.toKString()
    }
    free(r)
    return str
}
