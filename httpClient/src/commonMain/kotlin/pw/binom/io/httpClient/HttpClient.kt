package pw.binom.io.httpClient

import kotlinx.coroutines.Dispatchers
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.crypto.Sha1MessageDigest
import pw.binom.io.AsyncChannel
import pw.binom.io.Closeable
import pw.binom.io.IOException
import pw.binom.io.http.HTTPMethod
import pw.binom.io.http.HashHeaders2
import pw.binom.io.http.Headers
import pw.binom.io.http.emptyHeaders
import pw.binom.io.http.websocket.HandshakeSecret
import pw.binom.io.http.websocket.InvalidSecurityKeyException
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.http.websocket.WebSocketConnectionImpl2
import pw.binom.io.httpClient.protocol.ConnectFactory2
import pw.binom.io.httpClient.protocol.ProtocolSelector
import pw.binom.io.httpClient.protocol.ProtocolSelectorBySchema
import pw.binom.io.httpClient.protocol.httpproxy.HttpProxyConnectFactory2
import pw.binom.io.httpClient.protocol.ssl.HttpSSLConnectFactory2
import pw.binom.io.httpClient.protocol.v11.Http11ConnectFactory2
import pw.binom.network.Network
import pw.binom.network.NetworkManager
import pw.binom.url.URL

class WebSocketRequest(val url: URL, val method: String, val masking: Boolean, val client: HttpClient) {
    val headers = HashHeaders2()
    private var started = false

    suspend fun start(): WebSocketConnection {
        check(!started) { "Connection already started" }
        started = true
        val requestKey = HandshakeSecret.generateRequestKey()
        val responseKey = HandshakeSecret.generateResponse(Sha1MessageDigest(), requestKey)
        headers[Headers.SEC_WEBSOCKET_KEY] = requestKey

        val request = client.startConnect(method = method, uri = url, headers = headers, keepAlive = false)
        val resp = request.flush()
        if (resp.responseCode != 101) {
            throw IOException("Invalid Response code: ${resp.responseCode}")
        }
        val respKey = resp.headers.getSingleOrNull(Headers.SEC_WEBSOCKET_ACCEPT)
            ?: throw IOException("Invalid Server Response. Missing header \"${Headers.SEC_WEBSOCKET_ACCEPT}\"")
        if (respKey != responseKey) {
            throw InvalidSecurityKeyException()
        }

        val connection = WebSocketConnectionImpl2 { }
        connection.reset(input = request.input, output = request.output, masking = masking)
        return connection
    }
}

fun HttpClient.connectWebSocket(
    uri: URL,
    headers: Headers = emptyHeaders(),
    masking: Boolean = true,
    method: String = "GET",
): WebSocketRequest {
    val wsRequest = WebSocketRequest(
        client = this,
        url = uri,
        method = method,
        masking = masking,
    )
    wsRequest.headers[Headers.HOST] = uri.host + (uri.port?.let { ":$it" } ?: "")
    wsRequest.headers[Headers.CONNECTION] = Headers.UPGRADE
    wsRequest.headers[Headers.UPGRADE] = Headers.WEBSOCKET
    wsRequest.headers[Headers.SEC_WEBSOCKET_VERSION] = "13"
    wsRequest.headers.add(headers)
    return wsRequest
}

suspend fun HttpClient.connectTcp(
    uri: URL,
    headers: Headers,
    method: String = "GET",
): AsyncChannel {
    val h = HashHeaders2(headers)
    h[Headers.CONNECTION] = Headers.UPGRADE
    h[Headers.UPGRADE] = Headers.TCP
    val request = startConnect(method = method, uri = uri, headers = headers, keepAlive = false)
    val resp = request.flush()
    if (resp.responseCode != 101) {
        throw IOException("Invalid Response code: ${resp.responseCode}")
    }
    HttpMetrics.defaultHttpRequestCountMetric.dec()
    return AsyncChannel.create(
        input = request.input,
        output = request.output,
    )
//    return request.channel.channel
}

interface HttpClient : Closeable {
//    val networkDispatcher: NetworkDispatcher

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
    var protocolSelector = ProtocolSelectorBySchema()
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
