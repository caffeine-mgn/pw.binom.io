package pw.binom.io.httpClient

import pw.binom.URL
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.ssl.KeyManager
import pw.binom.ssl.TrustManager

class WSClient private constructor(val manager: SocketNIOManager, val keyManager: KeyManager, val trustManager: TrustManager) {
    companion object {
        fun connect(manager: SocketNIOManager, url: URL,
                    keyManager: KeyManager = EmptyKeyManager,
                    trustManager: TrustManager = TrustManager.TRUST_ALL) {

        }
    }
}