package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.concurrency.DeadlineTimer
import pw.binom.net.URI
import pw.binom.io.AsyncChannel
import pw.binom.io.Closeable
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.HTTPMethod
import pw.binom.io.socket.ssl.AsyncSSLChannel
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.network.TcpConnection
import pw.binom.ssl.KeyManager
import pw.binom.ssl.SSLContext
import pw.binom.ssl.SSLMethod
import pw.binom.ssl.TrustManager
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class BaseHttpClient(
    val networkDispatcher: NetworkDispatcher,
    val useKeepAlive: Boolean = true,
    keyManager: KeyManager = EmptyKeyManager,
    trustManager: TrustManager = TrustManager.TRUST_ALL,
    val bufferSize: Int = DEFAULT_BUFFER_SIZE
) : HttpClient {
    internal val deadlineTimer = DeadlineTimer()
    private val sslContext: SSLContext = SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)
    private val connections = HashMap<String, ArrayList<AsyncAsciiChannel>>()

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
        return AsyncAsciiChannel(channel = channel, bufferSize = bufferSize)
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
        deadlineTimer.close()
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