package pw.binom.io.httpClient

import kotlinx.coroutines.Dispatchers
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.Closeable
import pw.binom.io.http.HTTPMethod
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.protocol.ConnectFactory2
import pw.binom.io.httpClient.protocol.ProtocolSelector
import pw.binom.io.httpClient.protocol.ProtocolSelectorBySchema
import pw.binom.io.httpClient.protocol.httpproxy.HttpProxyConnectFactory2
import pw.binom.io.httpClient.protocol.ssl.HttpSSLConnectFactory2
import pw.binom.io.httpClient.protocol.v11.Http11ConnectFactory2
import pw.binom.network.Network
import pw.binom.network.NetworkManager
import pw.binom.url.URL

interface HttpClient : Closeable {

    suspend fun startConnect(
        method: String,
        uri: URL,
        headers: Headers,
        requestLength: OutputLength = OutputLength.Chunked,
        keepAlive: Boolean? = true,
    ): HttpRequestBody

    suspend fun connect(method: String, uri: URL): HttpRequest
    suspend fun connect(method: HTTPMethod, uri: URL) =
        connect(method.code, uri)

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
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    bufferCapacity: Int = 16,
    proxyURL: URL? = null,
): BaseHttpClient {
    val baseProtocolSelector = ProtocolSelectorBySchema()
    val http = Http11ConnectFactory2(networkManager = networkDispatcher)
    baseProtocolSelector.set(
        http,
        "http",
        "ws",
    )
    val protocolSelector = ProtocolSelectorBySchema()
    protocolSelector.set(
        HttpSSLConnectFactory2(networkManager = networkDispatcher, protocolSelector = baseProtocolSelector),
        "https",
        "wss",
    )
    protocolSelector.set(
        http,
        "http",
        "ws",
    )
    var pp: ProtocolSelector = protocolSelector
    if (proxyURL != null) {
        val proxyFactory = HttpProxyConnectFactory2(
            proxyUrl = proxyURL,
            networkManager = networkDispatcher,
            protocolSelector = protocolSelector,
        )
        pp = object : ProtocolSelector {
            override fun find(url: URL): ConnectFactory2? = proxyFactory
        }
    }
    return BaseHttpClient(
        useKeepAlive = useKeepAlive,
        bufferSize = bufferSize,
        bufferCapacity = bufferCapacity,
        requestHook = proxyURL?.let { RequestHook.HttpProxy(it) } ?: RequestHook.Default,
        protocolSelector = pp,
    )
}
