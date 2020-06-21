package pw.binom.io.socket.ssl

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.io.InputStream
import pw.binom.io.OutputStream
import pw.binom.io.socket.NativeSocketHolder
import pw.binom.io.socket.RawSocket
import pw.binom.io.socket.Socket
import pw.binom.io.socket.SocketClosedException
import pw.binom.ssl.SSLContext
import pw.binom.thread.Thread
import kotlin.native.concurrent.freeze
import kotlin.native.internal.NativePtr.Companion.NULL

/*val pair by lazy {
    CertificateGenerator.generate(
            2048,
            CertificateAlgorithm.RSA,
            Date.now(),
            Date.now(),
            mapOf(),
            10,
            "DC=name",
            "CN=example"
    )
}

val private_cer by lazy {
    pair.first
}

val public_cer by lazy {
    pair.second
}*/

/*
actual class SSLSocket(val ctx: CPointer<SSL_CTX>, val raw: RawSocket) : Socket {
    override val native: NativeSocketHolder
        get() = raw.native

    val rawSocket: Socket
        get() = raw

    private val sslIN = SSLInputStream()
    override val input: InputStream = sslIN//.buffered()
    override val output: OutputStream = SSLOutputStream()

    //    private lateinit var ctx: CPointer<SSL_CTX>
    lateinit var ssl: CPointer<SSL>

    constructor(ctx: SSLContext) : this(ctx.client(), RawSocket())


    private inner class SSLOutputStream : OutputStream {
        override fun write(data: ByteArray, offset: Int, length: Int): Int {
            if (closed)
                throw SocketClosedException()
//            if (!checkState())
//                return 0
            if (data.isEmpty()) {
                return 0
            }
            if (offset + length > data.size)
                throw IndexOutOfBoundsException("write(offset=$offset length=$length data.size=${data.size})")
            val r = SSL_write(ssl, data.refTo(offset), length)
            if (r <= 0) {
                val ret = SSL_get_error(ssl, r)
                TODO("SSL_write $r $ret")
            }
            return length
        }

        override fun flush() {
        }

        override fun close() {
        }
    }

    private inner class SSLInputStream : InputStream {
        override fun read(data: ByteArray, offset: Int, length: Int): Int {
            if (closed)
                throw SocketClosedException()
//            if (!checkState())
//                return 0
            if (data.isEmpty()) {
                return 0
            }
            if (offset + length > data.size)
                throw IndexOutOfBoundsException("read(offset=$offset length=$length data.size=${data.size})")

            val r = SSL_read(ssl, data.refTo(offset), length)
            if (r <= 0) {
                val ret = SSL_get_error(ssl, r)
                if (ret == SSL_ERROR_WANT_READ) {
                    return -5
                }
                this@SSLSocket.close()
                throw SocketClosedException()
            }
            return r
        }

        override fun close() {
        }

    }

    private val rbio = BIO_new(BIO_s_mem()!!)!!
    private val wbio = BIO_new(BIO_s_mem()!!)!!

    init {
        SSL_CTX_set_verify(ctx, SSL_VERIFY_NONE, null)

        ssl = SSL_new(ctx)!!
        SSL_set_bio(ssl, rbio, wbio)
        freeze()
    }

    fun CPointer<SSL>.get_cipher_list(): List<String> {
        val out = ArrayList<String>()
        memScoped {
            var n = 0
            var nn: ByteVar?
            do {
                nn = SSL_get_cipher_list(this@get_cipher_list, n)?.pointed ?: break
                n++
                out += nn.ptr.toKString()
            } while (nn.rawPtr !== NULL)
        }
        return out
    }

    //    lateinit var ctx: CPointer<SSL_CTX>

//    lateinit var bio_remote: CPointer<BIO>

*/
/*
    fun checkState(): Boolean {
        if (SSL_is_init_finished(ssl) != 1) {
            if (SSL_accept(ssl) != -1)
                return true
            return false
        }
        return true
    }
*//*



    internal fun accepted(): Boolean {
        val b = raw.blocking
        val start = Thread.currentTimeMillis()
        while (true) {
            if (Thread.currentTimeMillis() - start > 1000) {
                return false
            }
            raw.blocking = false
            val r = SSL_accept(ssl)
            if (r == -1) {
                val ret = SSL_get_error(ssl, r)
                if (ret != SSL_ERROR_WANT_READ && ret != SSL_ERROR_WANT_WRITE) {
                    close()
                    return false
                }
            }
            if (r != -1)
                break
        }
        raw.blocking = b
        return true
    }

    override fun connect(host: String, port: Int) {
        raw.connect(host, port)
        SSL_set_connect_state(ssl)
//        internal_BIO_set_conn_hostname(bio_remote, "$host:$port".cstr)
        if (SSL_set1_host(ssl, "$host:$port") <= 0) {
            TODO("SSL_set1_host error")
        }
        SSL_ctrl(ssl, SSL_CTRL_SET_TLSEXT_HOSTNAME, TLSEXT_NAMETYPE_host_name, "$host:$port".cstr)
//        internal_BIO_do_connect()
        val r = SSL_connect(ssl)
        if (r < 0) {
            val ret = SSL_get_error(ssl, r)
//            val r = getSSLError(ret)!!
//
//            memScoped {
//                println("ERRRRRROR: ${r.pointed.ptr.toKString()}")
//            }
//            free(r)
            TODO("SSL_connect error  $ret   $r")
        }
    }

    override val connected: Boolean
        get() = raw.connected
    override val closed: Boolean
        get() = raw.closed

    override fun close() {
        if (!closed) {
            raw.close()
            BIO_free(wbio)
            BIO_free(rbio)
            SSL_shutdown(ssl)
            SSL_free(ssl)
            SSL_CTX_free(ctx)
        }
    }
}*/
