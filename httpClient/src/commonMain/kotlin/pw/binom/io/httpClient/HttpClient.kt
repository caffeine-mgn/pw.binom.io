package pw.binom.io.httpClient

import pw.binom.URL
import pw.binom.io.AsyncChannel
import pw.binom.io.Closeable
import pw.binom.io.http.HTTPMethod
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.ssl.KeyManager
import pw.binom.ssl.SSLContext
import pw.binom.ssl.SSLMethod
import pw.binom.ssl.TrustManager

class HttpClient(
    val networkDispatcher: NetworkDispatcher,
    val useKeepAlive: Boolean = true,
    keyManager: KeyManager = EmptyKeyManager,
    trustManager: TrustManager = TrustManager.TRUST_ALL
) : Closeable {
    private val sslContext: SSLContext = SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)
    private val connections = HashMap<String, ArrayList<AsyncAsciiChannel>>()

    fun recycleConnection(url: URL, channel: AsyncAsciiChannel) {
        connections.getOrPut(url.asKey) { ArrayList() }.add(channel)
    }

    private suspend fun borrowConnection(url: URL): AsyncAsciiChannel {
        val id = url.asKey
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
        val port = url.getPort()
        var channel: AsyncChannel = networkDispatcher.tcpConnect(
            NetworkAddress.Immutable(
                host = url.host,
                port = port
            )
        )

        if (url.protocol == "https" || url.protocol == "wss") {
            val sslSession = sslContext.clientSession(host = url.host, port = port)
            channel = sslSession.asyncChannel(channel)
        }
        return AsyncAsciiChannel(channel)
    }

    suspend fun request(method: HTTPMethod, url: URL): HttpRequest {
        val protocol = url.protocol ?: throw IllegalArgumentException("URL \"$url\" must contains protocol")
        if (protocol != "http" && protocol != "https" && protocol != "ws" && protocol != "wss") {
            throw IllegalArgumentException("Protocol ${url.protocol} not supported")
        }
        val connect = borrowConnection(url)
        return DefaultHttpRequest(
            url = url,
            client = this,
            channel = connect,
            method = method
        )
    }

    private val URL.asKey
        get() = "${protocol ?: ""}://${host}:${port}"

    private fun URL.getPort() =
        port ?: when (protocol) {
            "ws", "http" -> 80
            "wss", "https" -> 443
            else -> throw IllegalArgumentException("Unknown default port for $this")
        }

    override fun close() {
        sslContext.close()
    }
}