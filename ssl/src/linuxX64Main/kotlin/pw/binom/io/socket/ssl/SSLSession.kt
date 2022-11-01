package pw.binom.io.socket.ssl

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.atomic.AtomicBoolean
import pw.binom.checkTrue
import pw.binom.io.ByteBuffer
import pw.binom.io.Closeable
import pw.binom.ssl.*

internal fun assertError(ssl: CPointer<SSL>, ret: Int) {
    if (ret <= 0 && SSL_get_error(ssl, ret) == SSL_ERROR_SSL) {
        getLastError()
        throw RuntimeException(ERR_error_string(SSL_get_error(ssl, ret).convert(), null)?.toKString())
    }
}

fun getLastError() = ERR_error_string(ERR_peek_last_error(), null)?.toKString()

actual class SSLSession(
    ctx: CPointer<SSL_CTX>,
    ssl: CPointer<SSL>,
    val client: Boolean,
    val trustManager: TrustManager,
    val keyManager: KeyManager
) : Closeable {
    actual enum class State {
        OK, WANT_WRITE, WANT_READ, ERROR, CLOSED
    }

    private val self = StableRef.create(this)

    init {
        SSLMetrics.incSSLSession()
        if (client) {
            SSL_CTX_set_verify(ctx, SSL_VERIFY_PEER, null)
            SSL_CTX_set_cert_verify_callback(ctx, sslServerCheck.reinterpret(), self.asCPointer())
        } else {
            SSL_CTX_callback_ctrl(
                ctx,
                SSL_CTRL_SET_TLSEXT_SERVERNAME_CB,
                sslHostCheck.reinterpret()
            ).convert<Int>().checkTrue("SSL_CTRL_SET_TLSEXT_SERVERNAME_CB error")
            SSL_CTX_ctrl(ctx, SSL_CTRL_SET_TLSEXT_SERVERNAME_ARG, 0, self.asCPointer()).convert<Int>()
                .checkTrue("SSL_CTRL_SET_TLSEXT_SERVERNAME_ARG error")
        }
    }

    actual class Status(actual val state: State, actual val bytes: Int) {
        override fun toString(): String {
            return "Status(state: [$state], bytes: [$bytes])"
        }
    }

    private class Resource(
        val ctx: CPointer<SSL_CTX>,
        val ssl: CPointer<SSL>,
    ) {
        private var disposed = false
        val rbio = BIO_new(BIO_s_mem()!!)!!
        val wbio = BIO_new(BIO_s_mem()!!)!!
        fun dispose() {
            if (disposed) {
                throw IllegalStateException("Already disposed!")
            }
            SSL_CTX_free(ctx)
            SSL_free(ssl)
//            BIO_free(rbio)
//            BIO_free(wbio)
        }
    }

    private val resource = Resource(ctx = ctx, ssl = ssl)

    private fun init(): State? {
        if (SSL_is_init_finished(resource.ssl) != 0) {
            return null
        }
        if (client) {
            while (true) {
                val n = SSL_connect(resource.ssl)
                if (n > 0) {
                    break
                }
                assertError(resource.ssl, n)
                val err = SSL_get_error(resource.ssl, n)
                if (err == SSL_ERROR_WANT_WRITE) {
                    return State.WANT_WRITE
                }

                if (err == SSL_ERROR_WANT_READ) {
                    return State.WANT_READ
                }
            }
        } else {
            val n = SSL_accept(resource.ssl)
            assertError(resource.ssl, n)
            return when (SSL_get_error(resource.ssl, n)) {
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
        SSL_set_bio(ssl, resource.rbio, resource.wbio)
    }

    actual fun readNet(dst: ByteArray, offset: Int, length: Int): Int {
        val n = BIO_read(resource.wbio, dst.refTo(0), dst.size)
        if (n < 0) {
            return 0
        }
        return n
    }

    actual fun writeNet(dst: ByteArray, offset: Int, length: Int): Int {
        var len = length
        var off = offset
        var readed = 0

        while (len > 0) {
            val n = BIO_write(resource.rbio, dst.refTo(off), len)
            if (n <= 0) {
                TODO()
            }
            readed += n

            off += n
            len -= n
        }
        return readed
    }

    actual fun writeApp(src: ByteArray, offset: Int, length: Int): Status {
        val r = init()
        if (r != null) {
            return Status(
                r,
                0
            )
        }
        val n = SSL_write(resource.ssl, src.refTo(offset), length)
        if (n > 0) {
            return Status(
                State.OK,
                n
            )
        }
        val state = when (val e = SSL_get_error(resource.ssl, n)) {
            SSL_ERROR_WANT_READ -> State.WANT_READ
            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
            SSL_ERROR_SSL -> State.ERROR
            else -> TODO("Unknown status $e")
        }
        return Status(
            state,
            0
        )
    }

    actual fun readNet(dst: ByteBuffer): Int {
        val n = dst.ref { dstPtr, remaining ->
            BIO_read(resource.wbio, dstPtr, remaining)
        } ?: 0
        if (n < 0) {
            return 0
        }
        dst.position += n
        return n
    }

    actual fun writeNet(dst: ByteBuffer): Int {
        var len = dst.remaining
        var off = dst.position
        var readed = 0

        while (len > 0) {
            val n = dst.refTo(off) { dstPtr ->
                BIO_write(resource.rbio, dstPtr, len)
            } ?: 0
            if (n <= 0) {
                TODO()
            }
            readed += n

            off += n
            len -= n
        }
        dst.position += readed
        return readed
    }

    actual fun readApp(dst: ByteBuffer): Status {
        val r = init()
        if (r != null) {
            return Status(
                state = r,
                bytes = 0
            )
        }
        if (!dst.isReferenceAccessAvailable()) {
            return Status(
                state = State.WANT_WRITE,
                bytes = 0,
            )
        }
        val n = dst.ref { dstPtr, remaining ->
            SSL_read(resource.ssl, dstPtr, dst.remaining)
        } ?: 0
        if (n > 0) {
            dst.position += n
            return Status(
                State.OK,
                n
            )
        }
        val state = when (val e = SSL_get_error(resource.ssl, n)) {
            SSL_ERROR_WANT_READ -> State.WANT_READ
            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
            SSL_ERROR_SSL -> State.ERROR
            SSL_ERROR_ZERO_RETURN -> State.CLOSED
            else -> TODO("Unknown status $e on read")
        }
        return Status(
            state,
            0
        )
    }

    actual fun writeApp(src: ByteBuffer): Status {
        val r = init()
        if (r != null) {
            return Status(
                r,
                0
            )
        }
        if (!src.isReferenceAccessAvailable()) {
            return Status(
                State.WANT_WRITE,
                0
            )
        }
        val n = src.ref { srcPtr, remaining ->
            SSL_write(resource.ssl, srcPtr, remaining)
        } ?: 0
        if (n > 0) {
            src.position += n
            return Status(
                State.OK,
                n
            )
        }
        assertError(resource.ssl, n)
        val state = when (val e = SSL_get_error(resource.ssl, n)) {
            SSL_ERROR_WANT_READ -> State.WANT_READ
            SSL_ERROR_WANT_WRITE -> State.WANT_WRITE
            SSL_ERROR_SSL -> State.ERROR
            SSL_ERROR_ZERO_RETURN -> State.CLOSED
            else -> TODO("Unknown status $e on write")
        }
        return Status(
            state = state,
            bytes = 0,
        )
    }

    private var closed = AtomicBoolean(false)
    override fun close() {
        if (!closed.compareAndSet(expected = false, new = true)) {
            throw IllegalStateException("SSLSession already closed")
        }
        SSLMetrics.decSSLSession()
        resource.dispose()
        self.dispose()
    }
}

private val sslServerCheck = staticCFunction<CPointer<X509_STORE_CTX>, COpaquePointer, Int> { x509Ctx, arg ->
    val self = arg.asStableRef<SSLSession>().get()
    val cain = X509_STORE_CTX_get0_chain(x509Ctx) ?: X509_STORE_CTX_get1_chain(x509Ctx)

    val list = Array(1 + (cain?.size ?: 0)) {
        if (it == 0) {
            X509Certificate(X509_STORE_CTX_get0_cert(x509Ctx)!!)
        } else {
            X509Certificate(cain!![it - 1])
        }
    }

    if (self.trustManager.isServerTrusted(list)) {
        1
    } else {
        -1
    }
}

private val sslHostCheck = staticCFunction<CPointer<SSL>, CPointer<IntVar>, COpaquePointer, Int> { ssl, _, arg ->
    val self = arg.asStableRef<SSLSession>().get()
    val hostName = SSL_get_servername(ssl, TLSEXT_NAMETYPE_host_name)?.toKString()
    val private = self.keyManager.getPrivate(hostName)
    val public = self.keyManager.getPublic(hostName)
    if (private != null) {
        SSL_use_PrivateKey(ssl, private.native).checkTrue("SSL_use_PrivateKey private")
    }
    if (public != null) {
        SSL_use_certificate(ssl, public.ptr).checkTrue("SSL_use_certificate error")
    }

    SSL_TLSEXT_ERR_OK
}
