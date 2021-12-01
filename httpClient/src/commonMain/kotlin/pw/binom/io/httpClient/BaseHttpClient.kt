package pw.binom.io.httpClient

import pw.binom.ByteBufferPool
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.concurrency.DeadlineTimer
import pw.binom.io.AsyncChannel
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.HTTPMethod
import pw.binom.io.socket.ssl.AsyncSSLChannel
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.net.URI
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkCoroutineDispatcher
import pw.binom.network.TcpConnection
import pw.binom.ssl.*
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class BaseHttpClient(
//    override val networkDispatcher: NetworkDispatcher,
    val networkDispatcher: NetworkCoroutineDispatcher,
    val useKeepAlive: Boolean = true,
    keyManager: KeyManager = EmptyKeyManager,
    trustManager: TrustManager = TrustManager.TRUST_ALL,
    val bufferSize: Int = DEFAULT_BUFFER_SIZE,
    bufferCapacity: Int = 16
) : HttpClient {
//    internal val deadlineTimer = DeadlineTimer.create()
    private val sslContext: SSLContext = SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)
    private val connections = HashMap<String, ArrayList<AsyncAsciiChannel>>()
    internal val textBufferPool = ByteBufferPool(capacity = bufferCapacity, size = bufferSize.toUInt())

    internal fun recycleConnection(URI: URI, channel: AsyncAsciiChannel) {
        connections.getOrPut(URI.asKey) { ArrayList() }.add(channel)
    }

    private suspend fun borrowConnection(uri: URI): AsyncAsciiChannel {
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
        var channel: AsyncChannel = networkDispatcher.tcpConnect(
            NetworkAddress.Immutable(
                host = uri.host,
                port = port
            )
        )

        if (uri.schema == "https" || uri.schema == "wss") {
            val sslSession = sslContext.clientSession(host = uri.host, port = port)
            channel = sslSession.asyncChannel(channel)
        }
        return AsyncAsciiChannel(pool = textBufferPool, channel = channel)
    }

    internal suspend fun interruptAndClose(channel: AsyncAsciiChannel) {
        var c = channel.channel
        while (c !is TcpConnection) {
            c = when (c) {
                is TcpConnection -> c
                is AsyncSSLChannel -> c.channel
                else -> TODO()
            }
        }
        c.interruptReading()
        channel.asyncClose()
    }

    override suspend fun connect(method: String, uri: URI, timeout: Duration?): HttpRequest {
        val schema = uri.schema ?: throw IllegalArgumentException("URL \"$uri\" must contains protocol")
        if (schema != "http" && schema != "https" && schema != "ws" && schema != "wss") {
            throw IllegalArgumentException("Schema ${uri.schema} is not supported")
        }
        val connect = borrowConnection(uri)
        return DefaultHttpRequest(
            uri = uri,
            client = this,
            channel = connect,
            method = method,
            timeout = timeout,
        )
    }

    override fun close() {
//        deadlineTimer.close()
        sslContext.close()
    }
}

private val URI.asKey
    get() = "${schema ?: ""}://${host}:${port}"

private fun URI.getPort() =
    port ?: when (schema) {
        "ws", "http" -> 80
        "wss", "https" -> 443
        else -> throw IllegalArgumentException("Unknown default port for $this")
    }

@OptIn(ExperimentalTime::class)
suspend fun BaseHttpClient.connect(method: HTTPMethod, uri: URI, timeout: Duration? = null) =
    connect(method.code, uri, timeout)