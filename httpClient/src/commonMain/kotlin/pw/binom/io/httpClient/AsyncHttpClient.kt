package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.AsyncOutput
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.URL
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncCloseable
import pw.binom.io.Closeable
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.socket.ssl.AsyncSSLChannel
import pw.binom.io.socket.ssl.SSLSession
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.ssl.*

object EmptyKeyManager : KeyManager {
    override fun getPrivate(serverName: String?): PrivateKey? = null

    override fun getPublic(serverName: String?): X509Certificate? = null

    override fun close() {
    }
}

open class AsyncHttpClient(val connectionManager: SocketNIOManager,
                           keyManager: KeyManager = EmptyKeyManager,
                           trustManager: TrustManager = TrustManager.TRUST_ALL
) : Closeable {

    val sslContext = SSLContext.getInstance(SSLMethod.TLSv1_2, keyManager, trustManager)

    override fun close() {
        connections.forEach {
            it.value.forEach {
                it.sslSession?.close()
                it.channel.unwrap().detach().close()
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

    class Connection(val sslSession: SSLSession?, val channel: AsyncChannel) : AsyncCloseable {
        override suspend fun close() {
            sslSession?.close()
            channel.close()
        }
    }

    private class AliveConnection(val sslSession: SSLSession?, val channel: AsyncChannel)

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
            var cc = channel.channel
//            if (url.protocol == "https")
//                cc = sslContext.clientSession(url.host, url.port ?: url.defaultPort!!).asyncChannel(cc.unwrap())
            return Connection(channel.sslSession, cc)
//            return Connection(channel.sslSession, channel.channel)
        }
        var connection = Connection(null, connectionManager.connect(
                host = url.host,
                port = port
        ))
        if (url.protocol == "https") {
            val sslSession = sslContext.clientSession(url.host, url.port ?: url.defaultPort!!)
            connection = Connection(sslSession, sslSession.asyncChannel(connection.channel))

        }
        return connection
    }

    internal fun recycleConnection(url: URL, connection: Connection) {
        cleanUp()
        val port = url.port ?: url.defaultPort ?: throw IllegalArgumentException("Unknown default port for $url")
        val key = "${url.protocol}://${url.host}:$port"
        val cc = connection.channel//.unwrap()//.detach()
        connections.getOrPut(key) { ArrayList() }.add(AliveConnection(connection.sslSession, cc))
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
/*
private class UrlConnectHTTP(val method: String, val url: URL, val client: AsyncHttpClient) : AsyncHttpClient.UrlConnect {

//    private suspend fun skipInput() {
//        try {
//            val buf = ByteArray(DEFAULT_BUFFER_SIZE)
//            while (true) {
//                if (inputStream.read(buf) <= 0)
//                    break
//            }
//
//            var r = 0
//            while (connect().inputAvailable) {
//                r += connect().input.read(buf)
//            }
//        } catch (e: StreamClosedException) {
//            //NOP
//        }
//    }

    override val inputStream = LazyAsyncInput {
        readResponse()
        val stream = when {
            responseHeaders[Headers.TRANSFER_ENCODING]?.any { it == Headers.CHUNKED }
                    ?: false -> AsyncChunkedInput(connect())
            responseHeaders[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull() != null ->
                AsyncContentLengthInput(connect(), responseHeaders[Headers.CONTENT_LENGTH]!!.single().toULong())
            else -> AsyncClosableInput(connect())
        }
        when (val contentEncode = responseHeaders[Headers.CONTENT_ENCODING]?.lastOrNull()?.toLowerCase()) {
            "deflate" -> AsyncInflateInput(stream = stream, wrap = true)
            "gzip" -> AsyncGZIPInput(stream)
            null -> stream
            else -> throw IOException("Unsupported Content Encode \"$contentEncode\"")
        }
    }

    override val outputStream = LazyAsyncOutput {
        sendRequest()
        when {
            requestHeaders[Headers.TRANSFER_ENCODING]?.any { it == Headers.CHUNKED } == true ->
                AsyncChunkedOutput(connect())
            requestHeaders[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull() != null ->
                AsyncContentLengthOutput(connect(), requestHeaders[Headers.CONTENT_LENGTH]!!.single().toULong())
            else -> throw IllegalStateException("Unknown Output Stream Type")
        }
    }


    override suspend fun responseCode(): Int {
        readResponse()
        return _responseCode
    }

    private var requestSend = false
    private var responseRead = false
    private var closed = false
    private var _responseCode = 0
    private val connectionKeepAlive: Boolean
        get() = responseHeaders[Headers.CONNECTION]?.singleOrNull() == Headers.KEEP_ALIVE

    private var requestHeaders = HashMap<String, ArrayList<String>>()
    private var responseHeaders = HashMap<String, ArrayList<String>>()

    override fun addRequestHeader(key: String, value: String) {
        if (requestSend)
            throw IllegalStateException("Headers already sended")
        requestHeaders.getOrPut(key) { ArrayList() }.add(value)
    }

    private suspend fun sendRequest() {
        try {
            if (requestSend) {
                return
            }
            val app = connect().utf8Appendable()
            app.append("$method ${url.uri} HTTP/1.1\r\n")

            if (!requestHeaders.containsKey(Headers.CONTENT_LENGTH))
                addRequestHeader(Headers.TRANSFER_ENCODING, Headers.CHUNKED)
            requestHeaders.forEach { en ->
                en.value.forEach {
                    app.append(en.key)
                    app.append(": ")
                    app.append(it)
                    app.append("\r\n")
                }
            }
            app.append("\r\n")
            connect().flush()
            requestSend = true
        } catch (e: Throwable) {
            e.stackTrace.forEach {
                println(it)
            }
            throw e
        }
    }

    private var socket: AsyncChannel? = null


    private suspend fun readResponse() {
        sendRequest()
        if (responseRead)
            return
        try {
            outputStream.close()
        } catch (e: StreamClosedException) {
            //NOP
        }

        val red = connect().utf8Reader()
        var responseLine: String
        while (true) {
            responseLine = red.readln() ?: ""
            if (responseLine.isNotEmpty())
                break
            break
        }
        _responseCode = responseLine.splitToSequence(' ').iterator().let {
            it.next()
            it.next()
        }.toInt()
        while (true) {
            val str = red.readln() ?: ""
            if (str.isEmpty()) {
                break
            }
            val items = str.split(": ")
            responseHeaders.getOrPut(items[0]) { ArrayList() }.add(items[1])
        }
        responseRead = true
    }

    private suspend fun connect(): AsyncChannel {
        if (closed)
            throw IllegalStateException("Connection already closed")
        if (socket != null)
            return socket!!
        if (socket == null) {
            val channel = client.pollConnection(url.protocol ?: "http", url.host, url.port ?: url.defaultPort!!)
            if (channel != null) {
                socket = client.connectionManager.attach(channel = channel, attachment = this)
                if (url.protocol == "https")
                    socket = client.sslContext.clientSession(url.host, url.port ?: url.defaultPort!!)
                            .asyncChannel(socket!!)
                if (url.protocol == "http") {
                    socket = AsyncBufferedChannel(socket!!)
                }
//                    socket = socket!!.ssl(client.sslContext, "${url.host}:${url.port ?: url.defaultPort}")
            }
        }

        if (socket == null) {
            val socketFactory = SocketFactory.rawSocketFactory
//            val socketFactory = when (url.protocol ?: "http") {
//                "ws", "http" -> SocketFactory.rawSocketFactory
//                "wss", "https" -> client.sslContext.socketFactory
//                else -> throw IllegalArgumentException("Not supported protocol ${url.protocol}")
//            }
            var con: AsyncChannel = client.connectionManager.connect(
                    host = url.host,
                    port = url.port ?: url.defaultPort!!,
                    attachment = this,
                    factory = socketFactory
            )
            if (url.protocol == "https") {
                con = client.sslContext.clientSession(url.host, url.port ?: url.defaultPort!!)
                        .asyncChannel(con)
//                con = con.ssl(client.sslContext, "${url.host}:${url.port ?: url.defaultPort}")
            }
            socket = con
        }
        return socket!!
    }

    override suspend fun close() {
        if (closed)
            return
        readResponse()
        val inputStream = inputStream.stream
        if (inputStream != null) {
            if (inputStream is AsyncContentLengthInputStream) {
                if (!inputStream.isEof) {
                    socket?.close()
                    socket = null
                    closed = true
                    return
                }
            }

        }
        closed = true

        val channel = socket?.unwrap()?.detach()
        socket = null
        if (channel?.isConnected == true) {
            if (connectionKeepAlive) {
                client.pushConnection(url.protocol ?: "http", url.host, url.port ?: url.defaultPort!!, channel)
            } else {
                channel.close()
            }
        }
    }

    override suspend fun getResponseHeaders(): Map<String, List<String>> {
        readResponse()
        return responseHeaders
    }
}*/

//fun AsyncChannel.unwrap(): SocketNIOManager.ConnectionRaw {
//    var c = this
//    while (true) {
//        when (c) {
//            is SocketNIOManager.ConnectionRaw -> return c
//            is AsyncSSLChannel -> c = return c//c.channel
//            else -> throw IllegalArgumentException()
//        }
//    }
//}

fun AsyncChannel.unwrap(): SocketNIOManager.ConnectionRaw {
    var c = this
    while (true) {
        when (c) {
            is SocketNIOManager.ConnectionRaw -> return c
            is AsyncSSLChannel -> c = c.channel
            else -> throw IllegalArgumentException()
        }
    }
}