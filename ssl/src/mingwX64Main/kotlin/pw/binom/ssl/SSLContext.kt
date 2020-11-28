package pw.binom.ssl

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.io.Closeable
import pw.binom.io.socket.ssl.SSLSession

private var inited = false
private val chiperList = "TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256:TLS_AES_128_GCM_SHA256:DHE-RSA-AES256-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384:DHE-DSS-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384:ECDHE-ECDSA-CHACHA20-POLY1305:ECDHE-RSA-CHACHA20-POLY1305:DHE-RSA-CHACHA20-POLY1305:ECDHE-ECDSA-AES256-CCM8:ECDHE-ECDSA-AES256-CCM:DHE-RSA-AES256-CCM8:DHE-RSA-AES256-CCM:ECDHE-ECDSA-ARIA256-GCM-SHA384:ECDHE-ARIA256-GCM-SHA384:DHE-DSS-ARIA256-GCM-SHA384:DHE-RSA-ARIA256-GCM-SHA384:ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:DHE-DSS-AES128-GCM-SHA256:DHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES128-CCM8:ECDHE-ECDSA-AES128-CCM:DHE-RSA-AES128-CCM8:DHE-RSA-AES128-CCM:ECDHE-ECDSA-ARIA128-GCM-SHA256:ECDHE-ARIA128-GCM-SHA256:DHE-DSS-ARIA128-GCM-SHA256:DHE-RSA-ARIA128-GCM-SHA256:ECDHE-ECDSA-AES256-SHA384:ECDHE-RSA-AES256-SHA384:DHE-DSS-AES256-SHA256:ECDHE-ECDSA-CAMELLIA256-SHA384:ECDHE-RSA-CAMELLIA256-SHA384:DHE-RSA-CAMELLIA256-SHA256:DHE-DSS-CAMELLIA256-SHA256:ECDHE-ECDSA-AES128-SHA256:ECDHE-RSA-AES128-SHA256:DHE-RSA-AES128-SHA256:DHE-DSS-AES128-SHA256:ECDHE-ECDSA-CAMELLIA128-SHA256:ECDHE-RSA-CAMELLIA128-SHA256:DHE-RSA-CAMELLIA128-SHA256:DHE-DSS-CAMELLIA128-SHA256:ECDHE-ECDSA-AES256-SHA:ECDHE-RSA-AES256-SHA:DHE-RSA-AES256-SHA:DHE-DSS-AES256-SHA:DHE-RSA-CAMELLIA256-SHA:DHE-DSS-CAMELLIA256-SHA:ECDHE-ECDSA-AES128-SHA:ECDHE-RSA-AES128-SHA:DHE-RSA-AES128-SHA:DHE-DSS-AES128-SHA:DHE-RSA-CAMELLIA128-SHA:DHE-DSS-CAMELLIA128-SHA"

actual class SSLContext(method: SSLMethod, val keyManager: KeyManager, val trustManager: TrustManager) : Closeable {

    init {
        if (!inited) {
            OPENSSL_init_crypto(OPENSSL_INIT_ADD_ALL_CIPHERS.convert(), null)
            OPENSSL_init_ssl(0.convert(), null)
        }
        inited = true
    }

    override fun close() {
        keyManager.close()
        self.dispose()
    }

//    actual val socketFactory: SSLSocketFactory by lazy { SSLSocketFactory(this) }


    private val client_method: CPointer<SSL_METHOD> = when (method) {
        SSLMethod.TLS, SSLMethod.TLSv1_2 -> TLSv1_2_client_method()
        SSLMethod.TLSv1 -> TLSv1_client_method()
        SSLMethod.TLSv1_1 -> TLSv1_1_client_method()
        else -> TODO()
    }!!
    private val server_method: CPointer<SSL_METHOD> = when (method) {
        SSLMethod.TLS, SSLMethod.TLSv1_2 -> TLSv1_2_server_method()
        SSLMethod.TLSv1 -> TLSv1_server_method()
        SSLMethod.TLSv1_1 -> TLSv1_1_server_method()
        else -> TODO()
    }!!


    private val self = StableRef.create(this)//DetachedObjectGraph(TransferMode.UNSAFE) { keyManager }
    fun server(): CPointer<SSL_CTX> {
        val serverCtx = SSL_CTX_new(server_method)!!
        SSL_CTX_callback_ctrl(serverCtx, SSL_CTRL_SET_TLSEXT_SERVERNAME_CB, sslHostCheck().reinterpret())
        SSL_CTX_ctrl(serverCtx, SSL_CTRL_SET_TLSEXT_SERVERNAME_ARG, 0, self.asCPointer())
        if (SSL_CTX_set_cipher_list(serverCtx, chiperList) != 1)
            TODO("SSL_CTX_set_cipher_list error")
        return serverCtx
    }

    fun client(): CPointer<SSL_CTX> {
        val clientCtx = SSL_CTX_new(client_method)!!
        if (SSL_CTX_set_cipher_list(clientCtx, chiperList) != 1)
            TODO("SSL_CTX_set_cipher_list error")

        SSL_CTX_set_verify(clientCtx, SSL_VERIFY_PEER, null)
        SSL_CTX_set_cert_verify_callback(clientCtx, sslServerCheck().reinterpret(), self.asCPointer())
        return clientCtx
    }

    actual companion object {
        actual fun getInstance(method: SSLMethod, keyManager: KeyManager, trustManager: TrustManager): SSLContext =
                SSLContext(method, keyManager, trustManager)
    }

    actual fun clientSession(host: String, port: Int): SSLSession {
        val sslCtx = client()
        SSL_CTX_set_verify(sslCtx, SSL_VERIFY_NONE, null)
        val ssl = SSL_new(sslCtx)!!
        val connect = "$host:$port"
        if (SSL_set1_host(ssl, connect) <= 0) {
            TODO("SSL_set1_host error")
        }
        if (SSL_ctrl(ssl, SSL_CTRL_SET_TLSEXT_HOSTNAME, TLSEXT_NAMETYPE_host_name.convert(), connect.cstr) <= 0)
            TODO("SSL_ctrl error")
        return SSLSession(sslCtx, ssl, true)
    }

    actual fun serverSession(): SSLSession {
        val sslCtx = client()
        SSL_CTX_set_verify(sslCtx, SSL_VERIFY_NONE, null)
        val ssl = SSL_new(sslCtx)!!
        return SSLSession(sslCtx, ssl, false)
    }
}

private fun sslServerCheck() = staticCFunction<CPointer<X509_STORE_CTX>, COpaquePointer, Int> { x509Ctx, arg ->
    val self = arg.asStableRef<SSLContext>().get()
    val cain = X509_STORE_CTX_get0_chain(x509Ctx) ?: X509_STORE_CTX_get1_chain(x509Ctx)

    val list = Array(1 + (cain?.size ?: 0)) {
        if (it == 0)
            X509Certificate(X509_STORE_CTX_get0_cert(x509Ctx)!!)
        else
            X509Certificate(cain!![it - 1])
    }

    if (self.trustManager.isServerTrusted(list))
        1
    else
        -1
}

private fun sslHostCheck() = staticCFunction<CPointer<SSL>, CPointer<IntVar>, COpaquePointer, Int> { ssl, ad, arg ->
    val self = arg.asStableRef<SSLContext>().get()
    val hostName = SSL_get_servername(ssl, TLSEXT_NAMETYPE_host_name)?.toKString()
    val private = self.keyManager.getPrivate(hostName)
    val public = self.keyManager.getPublic(hostName)
    if (private != null) {
        if (SSL_use_PrivateKey(ssl, private.native) < 0)
            TODO("SSL_use_PrivateKey private")
    }
    if (public != null) {
        if (SSL_use_certificate(ssl, public.ptr) < 0)
            TODO("SSL_use_certificate error")
    }

    SSL_TLSEXT_ERR_OK
}