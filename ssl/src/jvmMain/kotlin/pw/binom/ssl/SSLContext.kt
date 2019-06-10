package pw.binom.ssl

import pw.binom.io.Closeable
import pw.binom.io.socket.ssl.SSLSocketFactory
import java.security.SecureRandom
import javax.net.ssl.SSLContext as JSSLContext
import javax.net.ssl.SSLSessionContext as JSSLSessionContext


actual class SSLContext private constructor(val ctx: JSSLContext, keyManager: KeyManager, trustManager: TrustManager) : Closeable {

    override fun close() {
    }

    actual val socketFactory: SSLSocketFactory = SSLSocketFactory(this)

    init {
        ctx.init(arrayOf(BinomX509KeyManager(keyManager)), arrayOf(BinomX509TrustManager(trustManager)), SecureRandom())
    }

    actual companion object {
        actual fun getInstance(method: SSLMethod, keyManager: KeyManager, trustManager: TrustManager): SSLContext {
            var protocol = when (method) {
                SSLMethod.TLS,
                SSLMethod.TLSv1_2 -> "TLSv1.2"
                SSLMethod.TLSv1_1 -> "TLSv1.1"
                SSLMethod.TLSv1 -> "TLSv1.0"
                SSLMethod.SSL, SSLMethod.SSLv2 -> "SSLv2.0"
            }
            return SSLContext(JSSLContext.getInstance(protocol), keyManager, trustManager)
        }
    }
}