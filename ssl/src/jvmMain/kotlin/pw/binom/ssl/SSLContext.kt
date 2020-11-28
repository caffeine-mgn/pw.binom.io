package pw.binom.ssl

import pw.binom.io.Closeable
//import pw.binom.io.socket.ssl.SSLServerSocketChannel
import pw.binom.io.socket.ssl.SSLSession
import pw.binom.io.socket.ssl.filterArray
import java.security.SecureRandom
import javax.net.ssl.SSLContext as JSSLContext
import javax.net.ssl.SSLSessionContext as JSSLSessionContext


actual class SSLContext private constructor(val ctx: JSSLContext, keyManager: KeyManager, trustManager: TrustManager) : Closeable {

    override fun close() {
    }

//    actual val socketFactory: SSLSocketFactory = SSLSocketFactory(this)

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

    actual fun clientSession(host: String, port: Int): SSLSession {
        val engine = ctx.createSSLEngine(host, port)
        engine.useClientMode = true
        engine.wantClientAuth = false//wantClientAuthentication
        engine.needClientAuth = false//needClientAuthentication
        engine.enabledProtocols = filterArray(engine.enabledProtocols, null, null)
        engine.enabledCipherSuites = filterArray(engine.enabledCipherSuites, null, null)
        return SSLSession(engine)
    }

    actual fun serverSession(): SSLSession {
        val engine = ctx.createSSLEngine()
        engine.useClientMode = false
        engine.wantClientAuth = false//wantClientAuthentication
        engine.needClientAuth = false//needClientAuthentication
        engine.enabledProtocols = filterArray(engine.enabledProtocols, null, null)
        engine.enabledCipherSuites = filterArray(engine.enabledCipherSuites, null, null)
        return SSLSession(engine)
    }
}