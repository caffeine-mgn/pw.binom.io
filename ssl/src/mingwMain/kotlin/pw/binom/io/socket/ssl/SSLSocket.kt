package pw.binom.io.socket.ssl

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.Date
import pw.binom.Thread
import pw.binom.io.InputStream
import pw.binom.io.OutputStream
import pw.binom.io.buffered
import pw.binom.io.socket.NativeSocketHolder
import pw.binom.io.socket.RawSocket
import pw.binom.io.socket.Socket
import pw.binom.io.socket.SocketClosedException
import pw.binom.ssl.SSLContext
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.isEmpty
import kotlin.collections.mapOf
import kotlin.collections.plusAssign
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

actual class SSLSocket(val ctx: CPointer<SSL_CTX>, val raw: RawSocket) : Socket {
    override val native: NativeSocketHolder
        get() = raw.native

    val rawSocket: Socket
        get() = raw

    private val sslIN = SSLInputStream()
    override val input: InputStream = sslIN.buffered()
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
                println("Write 0")
                return 0
            }
            if (offset + length > data.size)
                throw IndexOutOfBoundsException("write(offset=$offset length=$length data.size=${data.size})")
            SSL_write(ssl, data.refTo(offset), length)
            println("Wrote $length")
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
                println("Close connection!!!")
                this@SSLSocket.close()
                throw SocketClosedException()
            }
            return r
        }

        override fun close() {
        }

    }


    init {
//        val keyManager = KeyManager()//val rr = AtomicReference<KeyManager?>(null)
//        val b = DetachedObjectGraph(TransferMode.UNSAFE) { keyManager }

//        rr.value = keyManager


        //SSL_CTX_set_options(ctx, SSL_OP_NO_SSLv2.convert<UInt>() or SSL_OP_NO_SSLv3 or SSL_OP_NO_COMPRESSION)


//        SSL_CTX_use_certificate(ctx, public_cer.ptr)

//        SSL_CTX_set_default_passwd_cb(ctx, password_cb);
//        SSL_CTX_use_PrivateKey(ctx, private_cer.ptr);


//        val rsa = RSA_generate_key(512, RSA_F4, null, null);

//        SSL_CTX_set_tmp_rsa(ctx, rsa);
//        if (X509_verify(public_cer.ptr, private_cer.ptr) < 0) {
//            TODO("X509_verify error")
//        }
//        RSA_free(rsa);


//        ctx = ctx//if (server) sslCtx.server() else sslCtx.client()


//        val public_cer =sslCtx.keyManager!!.getPublic("localhost")!!
//        val private_cer =sslCtx.keyManager!!.getPrivate("localhost")!!

//        if (X509_verify(public_cer.ptr, private_cer.native) < 0) {
//            TODO("X509_verify error")
//        }

//        SSL_CTX_use_certificate(ctx, public_cer.ptr)
//        SSL_CTX_use_PrivateKey(ctx, private_cer.native)
        SSL_CTX_set_verify(ctx, SSL_VERIFY_NONE, null)

        ssl = SSL_new(ctx)!!
//        SSL_use_certificate(ssl, public_cer.ptr)
//        SSL_use_PrivateKey(ssl, private_cer.native)


//        if (SSL_CTX_use_certificate_chain_file(ctx, "D:\\Temp\\server-certificate-chain.pem") != 1)
//            TODO("SSL_CTX_use_certificate_chain_file error")
//        if (SSL_CTX_use_PrivateKey_file(ctx, "D:\\Temp\\server-private-key.pem", SSL_FILETYPE_PEM) != 1)
//            TODO("SSL_CTX_use_PrivateKey_file error")
//        if (SSL_CTX_check_private_key(ctx) != 1)
//            TODO("SSL_CTX_check_private_key error")
//        if (SSL_use_PrivateKey(ssl, private_cer.ptr) < 0) {
//            TODO("SSL_use_PrivateKey error")
//        }

//        bio_out = BIO_new_socket(raw.native.code, BIO_NOCLOSE)!!
//        bio_remote=BIO_new_ssl_connect(ctx)!!
        //SSL_set_bio(ssl, bio_out, bio_out)
//        SSL_set_cipher_list(ssl, "HIGH:!aNULL:!kRSA:!PSK:!SRP:!MD5:!RC4")
        SSL_set_fd(ssl, raw.native.code)
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

/*
    fun checkState(): Boolean {
        if (SSL_is_init_finished(ssl) != 1) {
            if (SSL_accept(ssl) != -1)
                return true
            return false
        }
        return true
    }
*/


    internal fun accepted(): Boolean {
        val b = raw.blocking
        val start = Thread.currentTimeMillis()
        while (true) {
            if (Thread.currentTimeMillis() - start > 1000) {
                println("Timeout")
                return false
            }
            raw.blocking = false
            val r = SSL_accept(ssl)
            println("accepted: $r")
            if (r == -1) {
                val ret = SSL_get_error(ssl, r)
                println("SSL_ERROR_WANT_READ=${ret == SSL_ERROR_WANT_READ}   SSL_ERROR_WANT_WRITE=${ret == SSL_ERROR_WANT_WRITE}   $ret")
                if (ret != SSL_ERROR_WANT_READ && ret != SSL_ERROR_WANT_WRITE) {
                    close()
                    return false
                }
            }
            if (r != -1)
                break
            Thread.sleep(1)
        }
        raw.blocking = b
        println("accepted!")
        return true
    }

    override fun connect(host: String, port: Int) {
        println("Connect to $host:$port...")
        println("connect->#1")
        raw.connect(host, port)
        println("connect->#2")
//        internal_BIO_set_conn_hostname(bio_remote, "$host:$port".cstr)
        SSL_set1_host(ssl, "$host:$port")
//        internal_BIO_do_connect()
        if (SSL_connect(ssl) < 0)
            TODO("SSL_connect error")
        println("connect->#4")
    }

    override val connected: Boolean
        get() = raw.connected
    override val closed: Boolean
        get() = raw.closed

    override fun close() {
        if (!closed) {
            raw.close()
            SSL_shutdown(ssl)
            SSL_free(ssl)
            SSL_CTX_free(ctx)
        }
    }
}