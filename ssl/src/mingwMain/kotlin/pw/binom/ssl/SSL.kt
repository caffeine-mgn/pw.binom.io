package pw.binom.ssl

import kotlinx.cinterop.CPointer
import platform.openssl.*
import pw.binom.io.Closeable
/*
private var ssl_inited = false
internal fun init_ssl() {
    if (ssl_inited)
        return
    SSL_library_init()
    ssl_inited = true
}

class SSLContext private constructor(val method: SSLMethods) {
    init {
        init_ssl()
    }

    enum class SSLMethods {
        SSL,
        SSLv2,
        SSLv3,
        TLS,
        TLSv1,
        TLSv1_1,
        TLSv1_2
    }

    companion object {
        fun getInstance(method: SSLMethods) = SSLContext(method)
    }

    fun getClientSessionContext(): SSLSessionContext =
            SSLSessionContext(when (method) {
                SSLMethods.SSL, SSLMethods.SSLv3 -> SSLv3_client_method()
                SSLMethods.SSLv2 -> SSLv23_client_method()
                SSLMethods.TLS, SSLMethods.TLSv1_2 -> TLSv1_2_client_method()
                SSLMethods.TLSv1 -> TLSv1_client_method()
                SSLMethods.TLSv1_1 -> TLSv1_1_client_method()
            }!!)

    fun getServerSessionContext(): SSLSessionContext =
            SSLSessionContext(when (method) {
                SSLMethods.SSL, SSLMethods.SSLv3 -> SSLv3_server_method()
                SSLMethods.SSLv2 -> SSLv23_server_method()
                SSLMethods.TLS, SSLMethods.TLSv1_2 -> TLSv1_2_server_method()
                SSLMethods.TLSv1 -> TLSv1_server_method()
                SSLMethods.TLSv1_1 -> TLSv1_1_server_method()
            }!!)
}

class SSLSessionContext(method: CPointer<SSL_METHOD>) : Closeable {

    val ctx = SSL_CTX_new(method)

    override fun close() {
        SSL_CTX_free(ctx)
    }
}
*/