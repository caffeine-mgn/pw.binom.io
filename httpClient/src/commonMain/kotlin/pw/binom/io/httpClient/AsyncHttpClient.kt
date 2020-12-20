package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.URL
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncCloseable
import pw.binom.io.Closeable
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.socket.ssl.AsyncSSLChannel
import pw.binom.io.socket.ssl.SSLSession
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.network.TcpConnection
import pw.binom.ssl.*

object EmptyKeyManager : KeyManager {
    override fun getPrivate(serverName: String?): PrivateKey? = null

    override fun getPublic(serverName: String?): X509Certificate? = null

    override fun close() {
    }
}

open class AsyncHttpClient(
    val connectionManager: NetworkDispatcher,
    keyManager: KeyManager = EmptyKeyManager,
    trustManager: TrustManager = TrustManager.TRUST_ALL
) : Closeable {

    val sslContext = SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)

    override fun close() {
        connections.forEach {
            it.value.forEach {
                it.sslSession?.close()
                it.channel.unwrap().close()
            }
        }
        connections.clear()
    }

    private val connections = HashMap<String, ArrayList<AliveConnection>>()

    private fun cleanUp() {
        val cit = connections.entries.iterator()
        while (cit.hasNext()) {
            val list = cit.next()

            val lit = list.value.iterator()
            while (lit.hasNext()) {
                val c = lit.next()
//                if (!c.channel.isConnected)
//                    lit.remove()
            }

            if (list.value.isEmpty())
                cit.remove()
        }
    }

    class Connection(val sslSession: SSLSession?, val channel: AsyncChannel, val rawConnection: TcpConnection) :
        AsyncCloseable {
        override suspend fun asyncClose() {
            sslSession?.close()
            channel.asyncClose()
        }
    }

    private class AliveConnection(
        val sslSession: SSLSession?,
        val channel: AsyncChannel,
        val rawConnection: TcpConnection
    )

    internal suspend fun borrowConnection(url: URL): Connection {
        cleanUp()
        val port = url.port ?: url.defaultPort ?: throw IllegalArgumentException("Unknown default port for $url")
        val key = "${url.protocol}://${url.host}:$port"
        var connectionList = connections[key]
        if (connectionList != null && !connectionList.isEmpty()) {
            val channel = connectionList.removeAt(connectionList.lastIndex)
//            val asyncChannel = channel.channel//connectionManager.attach()
//
//            val cc = channel.sslSession?.let { it.asyncChannel(asyncChannel) } ?: asyncChannel
//            var cc:AsyncChannel = asyncChannel
//            if (url.protocol == "https")
//                cc = sslContext.clientSession(url.host, url.port ?: url.defaultPort!!).asyncChannel(cc.unwrap())
            return Connection(channel.sslSession, channel.channel, channel.rawConnection)
//            return Connection(channel.sslSession, channel.channel)
        }
        val raw = connectionManager.tcpConnect(
            NetworkAddress.Immutable(
                host = url.host,
                port = port
            )
        )
        var connection = Connection(null, raw, raw)
        if (url.protocol == "https") {
            val sslSession = sslContext.clientSession(url.host, url.port ?: url.defaultPort!!)
            connection = Connection(sslSession, sslSession.asyncChannel(connection.channel), raw)

        }
        return connection
    }

    internal fun recycleConnection(url: URL, connection: Connection) {
        cleanUp()
        val port = url.port ?: url.defaultPort ?: throw IllegalArgumentException("Unknown default port for $url")
        val key = "${url.protocol}://${url.host}:$port"
        connections.getOrPut(key) { ArrayList() }
            .add(AliveConnection(connection.sslSession, connection.channel, connection.rawConnection))
    }

    /*
        internal fun pollConnection(proto: String, host: String, port: Int): SocketChannel? {
            cleanUp()
            val key = "$proto://$host:$port"
            val con = connections[key] ?: return null
            val i = con.indexOfFirst { it.isConnected }
            val r = con[i]
            con.removeAt(i)
            return r
        }

        internal fun pushConnection(proto: String, host: String, port: Int, socket: SocketChannel) {
            if (!socket.isConnected)
                return
            val key = "$proto://$host:$port"
            connections.getOrPut(key) { ArrayList() }.add(socket)
        }
    */
    fun request(method: String, url: URL, flushSize: Int = DEFAULT_BUFFER_SIZE): UrlConnect {
        return UrlConnectImpl(
            method = method,
            url = url,
            client = this,
            outputFlushSize = flushSize
        )
    }

//    fun request(method: String, url: URL): UrlConnect {
//        val r = when (url.protocol) {
//            "http", "https" -> UrlConnectHTTP(method, url, this)
//            else -> throw RuntimeException("Unknown protocol \"${url.protocol}\"")
//        }
//        r.addRequestHeader(Headers.USER_AGENT, "Binom Client")
//        r.addRequestHeader(Headers.CONNECTION, Headers.KEEP_ALIVE)
//        r.addRequestHeader(Headers.HOST, url.host)
//        r.addRequestHeader(Headers.ACCEPT_ENCODING, "gzip, deflate")
//        return r
//    }

    interface UrlConnect : AsyncCloseable {
        //        suspend fun responseCode(): Int
//        val inputStream: AsyncInput
//        val outputStream: AsyncOutput
        val headers: MutableMap<String, MutableList<String>>
        suspend fun upload(): UrlRequest
        suspend fun response(): UrlResponse
        suspend fun websocket(origin: String? = null): WebSocketConnection
        fun addHeader(key: String, value: String): UrlConnect {
            headers.getOrPut(key) { ArrayList() }.add(value)
            return this
        }
    }

    interface UrlRequest : AsyncOutput {
        suspend fun response(): UrlResponse
    }

    interface UrlResponse : AsyncInput {
        val responseCode: Int
        val headers: Map<String, List<String>>
    }
}

private fun AsyncChannel.unwrap(): TcpConnection {
    var c = this
    while (true) {
        when (c) {
            is TcpConnection -> return c
            is AsyncSSLChannel -> c = c.channel
            else -> throw IllegalArgumentException()
        }
    }
}