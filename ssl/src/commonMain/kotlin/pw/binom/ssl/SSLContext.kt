package pw.binom.ssl

import pw.binom.io.Closeable
import pw.binom.io.socket.ssl.SSLSession
//import pw.binom.io.socket.ssl.SSLSocketFactory

enum class SSLMethod {
    SSL,
    SSLv2,

    //    SSLv3,
    TLS,
    TLSv1,
    TLSv1_1,
    TLSv1_2
}

expect class SSLContext : Closeable {
//    val socketFactory: SSLSocketFactory

    fun clientSession(host: String, port: Int): SSLSession
    fun serverSession(): SSLSession

//    class SSLSessionContext : Closeable

    companion object {
        fun getInstance(method: SSLMethod, keyManager: KeyManager, trustManager: TrustManager): SSLContext
    }
}