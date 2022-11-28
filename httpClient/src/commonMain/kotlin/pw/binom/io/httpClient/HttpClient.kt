package pw.binom.io.httpClient

import kotlinx.coroutines.Dispatchers
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.Closeable
import pw.binom.network.Network
import pw.binom.network.NetworkManager
import pw.binom.ssl.EmptyKeyManager
import pw.binom.ssl.KeyManager
import pw.binom.ssl.TrustManager
import pw.binom.url.URL
import kotlin.time.ExperimentalTime

interface HttpClient : Closeable {
//    val networkDispatcher: NetworkDispatcher

    @OptIn(ExperimentalTime::class)
    suspend fun connect(method: String, uri: URL): HttpRequest

    companion object {
//        fun create(
//            networkDispatcher: NetworkCoroutineDispatcher,
//            useKeepAlive: Boolean = true,
//            keyManager: KeyManager = EmptyKeyManager,
//            trustManager: TrustManager = TrustManager.TRUST_ALL,
//            bufferSize: Int = DEFAULT_BUFFER_SIZE,
//            bufferCapacity: Int = 16
//        ) =
//            BaseHttpClient(
//                networkDispatcher = networkDispatcher,
//                useKeepAlive = useKeepAlive,
//                keyManager = keyManager,
//                trustManager = trustManager,
//                bufferSize = bufferSize,
//                bufferCapacity = bufferCapacity,
//            )
    }
}

fun HttpClient.Companion.create(
    networkDispatcher: NetworkManager = Dispatchers.Network,
    useKeepAlive: Boolean = true,
    keyManager: KeyManager = EmptyKeyManager,
    trustManager: TrustManager = TrustManager.TRUST_ALL,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    bufferCapacity: Int = 16,
    connectionFactory: ConnectionFactory = ConnectionFactory.DEFAULT
) =
    BaseHttpClient(
        networkDispatcher = networkDispatcher,
        useKeepAlive = useKeepAlive,
        keyManager = keyManager,
        trustManager = trustManager,
        bufferSize = bufferSize,
        bufferCapacity = bufferCapacity,
        connectionFactory = connectionFactory,
    )
