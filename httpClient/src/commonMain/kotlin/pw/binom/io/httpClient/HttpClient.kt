package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.Closeable
import pw.binom.net.URI
import pw.binom.network.NetworkDispatcher
import pw.binom.ssl.EmptyKeyManager
import pw.binom.ssl.KeyManager
import pw.binom.ssl.TrustManager
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

interface HttpClient: Closeable {
    val networkDispatcher: NetworkDispatcher

    @OptIn(ExperimentalTime::class)
    suspend fun connect(method: String, uri: URI, timeout: Duration? = null): HttpRequest

    companion object {
        fun create(
            networkDispatcher: NetworkDispatcher,
            useKeepAlive: Boolean = true,
            keyManager: KeyManager = EmptyKeyManager,
            trustManager: TrustManager = TrustManager.TRUST_ALL,
            bufferSize: Int = DEFAULT_BUFFER_SIZE
        ) =
            BaseHttpClient(
                networkDispatcher = networkDispatcher,
                useKeepAlive = useKeepAlive,
                keyManager = keyManager,
                trustManager = trustManager,
                bufferSize = bufferSize,
            )
    }
}