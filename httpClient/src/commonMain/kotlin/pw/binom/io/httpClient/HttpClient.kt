package pw.binom.io.httpClient

import kotlinx.coroutines.Dispatchers
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.Closeable
import pw.binom.net.URL
import pw.binom.network.Network
import pw.binom.network.NetworkCoroutineDispatcher
import pw.binom.ssl.EmptyKeyManager
import pw.binom.ssl.KeyManager
import pw.binom.ssl.TrustManager
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
    networkDispatcher: NetworkCoroutineDispatcher = Dispatchers.Network,
    useKeepAlive: Boolean = true,
    keyManager: KeyManager = EmptyKeyManager,
    trustManager: TrustManager = TrustManager.TRUST_ALL,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    bufferCapacity: Int = 16
) =
    BaseHttpClient(
        networkDispatcher = networkDispatcher,
        useKeepAlive = useKeepAlive,
        keyManager = keyManager,
        trustManager = trustManager,
        bufferSize = bufferSize,
        bufferCapacity = bufferCapacity,
    )
