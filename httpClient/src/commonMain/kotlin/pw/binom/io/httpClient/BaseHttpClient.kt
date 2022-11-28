package pw.binom.io.httpClient

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.binom.ByteBufferPool
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableMap
import pw.binom.collections.useName
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.HTTPMethod
import pw.binom.io.http.websocket.WebSocketConnectionPool
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.network.NetworkManager
import pw.binom.ssl.*
import pw.binom.url.URL
import kotlin.time.ExperimentalTime

class BaseHttpClient(
    val networkDispatcher: NetworkManager,
    val useKeepAlive: Boolean = true,
    keyManager: KeyManager = EmptyKeyManager,
    trustManager: TrustManager = TrustManager.TRUST_ALL,
    val bufferSize: Int = DEFAULT_BUFFER_SIZE,
    val sslBufferSize: Int = DEFAULT_BUFFER_SIZE,
    bufferCapacity: Int = 16,
    websocketMessagePoolSize: Int = 16,
    var connectionFactory: ConnectionFactory = ConnectionFactory.DEFAULT
) : HttpClient {
    init {
        HttpMetrics.baseHttpClientCountMetric.inc()
    }

    internal val webSocketConnectionPool by lazy { WebSocketConnectionPool(websocketMessagePoolSize) }
    private val sslContext: SSLContext by lazy {
        SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)
    }
    private val connections =
        defaultMutableMap<String, MutableList<AsyncAsciiChannel>>().useName("BaseHttpClient.connections")
    internal val textBufferPool = ByteBufferPool(capacity = bufferCapacity, bufferSize = bufferSize.toUInt())

    val idleSize
        get() = connections.map { it.value.size }.sum()

    internal fun recycleConnection(URI: URL, channel: AsyncAsciiChannel) {
//        GlobalScope.launch(networkDispatcher) {
//            try {
//                channel.asyncClose()
//                println("Connection closed success")
//            } catch (e: Throwable) {
//                println("Connection closed with error: $e:\n${e.stackTraceToString()}")
//            }
//        }
        connections.getOrPut(URI.asKey) { defaultMutableList() }.add(channel)
    }

    private suspend fun borrowConnection(uri: URL): AsyncAsciiChannel {
        val id = uri.asKey
        val list = connections[id]
        if (list != null) {
            val con = list.removeLastOrNull()
            if (con != null) {
                if (list.isEmpty()) {
                    connections.remove(id)
                }
                return con
            }
        }
        val port = uri.getPort()
        var channel = connectionFactory.connect(
            networkManager = networkDispatcher,
            schema = uri.schema,
            host = uri.host,
            port = port,
        )

        if (uri.schema == "https" || uri.schema == "wss") {
            val sslSession = sslContext.clientSession(host = uri.host, port = port)
            channel = sslSession.asyncChannel(channel, closeParent = true, bufferSize = sslBufferSize)
        }
        return AsyncAsciiChannel(pool = textBufferPool, channel = channel)
    }

    internal suspend fun interruptAndClose(channel: AsyncAsciiChannel) {
//        var c = channel.channel
//        while (c !is TcpConnection) {
//            c = when (c) {
//                is TcpConnection -> c
//                is AsyncSSLChannel -> c.channel
//                else -> TODO()
//            }
//        }
        channel.asyncClose()
    }

    override suspend fun connect(method: String, uri: URL): HttpRequest {
        var connect: AsyncAsciiChannel? = null
        try {
            val schema = uri.schema
            if (schema != "http" && schema != "https" && schema != "ws" && schema != "wss") {
                throw IllegalArgumentException("Schema ${uri.schema} is not supported")
            }
            connect = borrowConnection(uri)
            return DefaultHttpRequest(
                uri = uri,
                client = this,
                channel = connect,
                method = method,
            )
        } catch (e: Throwable) {
            runCatching { connect?.asyncClose() }
            throw e
        }
    }

    override fun close() {
        HttpMetrics.baseHttpClientCountMetric.dec()
//        deadlineTimer.close()
        GlobalScope.launch { connections.forEach { it.value.forEach { it.channel.asyncClose() } } }
        connections.clear()
    }
}

private val URL.asKey
    get() = "$schema://$host:$port"

private fun URL.getPort() =
    port ?: when (schema) {
        "ws", "http" -> 80
        "wss", "https" -> 443
        "ssh" -> 22
        "ftp" -> 21
        "rdp" -> 3389
        "vnc" -> 5900
        "telnet" -> 23
        else -> throw IllegalArgumentException("Unknown default port for $this")
    }

@OptIn(ExperimentalTime::class)
suspend fun BaseHttpClient.connect(method: HTTPMethod, uri: URL) =
    connect(method.code, uri)
