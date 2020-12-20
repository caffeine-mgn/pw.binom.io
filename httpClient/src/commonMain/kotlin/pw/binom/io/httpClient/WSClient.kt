package pw.binom.io.httpClient

import pw.binom.URL
import pw.binom.network.NetworkDispatcher
import pw.binom.ssl.KeyManager
import pw.binom.ssl.TrustManager

class WSClient private constructor(
    val manager: NetworkDispatcher,
    val keyManager: KeyManager,
    val trustManager: TrustManager
) {
    companion object {
        fun connect(
            manager: NetworkDispatcher, url: URL,
            keyManager: KeyManager = EmptyKeyManager,
            trustManager: TrustManager = TrustManager.TRUST_ALL
        ) {

        }
    }
}