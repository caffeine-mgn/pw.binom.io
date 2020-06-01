package pw.binom.io.httpClient

import pw.binom.URL
import pw.binom.io.*
import pw.binom.io.http.*
import pw.binom.io.socket.SocketChannel
import pw.binom.io.socket.SocketFactory
import pw.binom.io.socket.nio.SocketNIOManager
import pw.binom.io.socket.rawSocketFactory
import pw.binom.io.socket.ssl.AsyncSSLChannel
import pw.binom.io.socket.ssl.asyncChannel
import pw.binom.ssl.*
import pw.binom.stackTrace

object EmptyKeyManager : KeyManager {
    override fun getPrivate(serverName: String?): PrivateKey? = null

    override fun getPublic(serverName: String?): X509Certificate? = null

    override fun close() {
    }
}

class AsyncHttpClient(val connectionManager: SocketNIOManager) : Closeable {

    val sslContext = SSLContext.getInstance(SSLMethod.TLSv1_1, EmptyKeyManager, TrustManager.TRUST_ALL)

    override fun close() {
        connections.forEach {
            it.value.forEach {
                it.close()
            }
        }
    }

    private val connections = HashMap<String, ArrayList<SocketChannel>>()

    private fun cleanUp() {
        val cit = connections.entries.iterator()
        while (cit.hasNext()) {
            val list = cit.next()

            val lit = list.value.iterator()
            while (lit.hasNext()) {
                val c = lit.next()
                if (!c.isConnected)
                    lit.remove()
            }

            if (list.value.isEmpty())
                cit.remove()
        }
    }

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

    fun request(method: String, url: URL): UrlConnect {
        val r = when (url.protocol) {
            "http", "https" -> UrlConnectHTTP(method, url, this)
            else -> throw RuntimeException("Unknown protocol \"${url.protocol}\"")
        }
        r.addRequestHeader(Headers.USER_AGENT, "Binom Client")
        r.addRequestHeader(Headers.CONNECTION, Headers.KEEP_ALIVE)
        r.addRequestHeader(Headers.HOST, url.host)
        return r
    }

    interface UrlConnect : AsyncCloseable {
        suspend fun responseCode(): Int
        val inputStream: AsyncInputStream
        val outputStream: AsyncOutputStream

        fun addRequestHeader(key: String, value: String)
        suspend fun getResponseHeaders(): Map<String, List<String>>
    }
}

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

    override val inputStream = LazyAsyncInputStream {
        readResponse()
        when {
            responseHeaders[Headers.TRANSFER_ENCODING]?.any { it == Headers.CHUNKED }
                    ?: false -> AsyncChunkedInputStream(connect().input)
            responseHeaders[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull() != null ->
                AsyncContentLengthInputStream(connect().input, responseHeaders[Headers.CONTENT_LENGTH]!!.single().toULong())
            else -> AsyncClosableInputStream(connect().input)
        }
    }

    override val outputStream = LazyAsyncOutputStream {
        sendRequest()
        when {
            requestHeaders[Headers.TRANSFER_ENCODING]?.any { it == Headers.CHUNKED } == true ->
                AsyncChunkedOutputStream(connect().output)
            requestHeaders[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull() != null ->
                AsyncContentLengthOututStream(connect().output, requestHeaders[Headers.CONTENT_LENGTH]!!.single().toULong())
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
            connect().output.writeln("$method ${url.uri} HTTP/1.1")

            if (!requestHeaders.containsKey(Headers.CONTENT_LENGTH))
                addRequestHeader(Headers.TRANSFER_ENCODING, Headers.CHUNKED)
            requestHeaders.forEach { en ->
                en.value.forEach {
                    connect().output.write(en.key)
                    connect().output.write(": ")
                    connect().output.writeln(it)
                }
            }
            connect().output.writeln()
            connect().output.flush()
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
        println("Request sendded!")
        try {
            outputStream.close()
        } catch (e: StreamClosedException) {
            //NOP
        }

        println("Read headers...")
        var responseLine: String
        while (true) {
            responseLine = connect().input.readln()
            println("Readed header $responseLine")
            if (responseLine.isNotEmpty())
                break
            break
        }
        println("Header readed!")
        _responseCode = responseLine.splitToSequence(' ').iterator().let {
            it.next()
            it.next()
        }.toInt()
        while (true) {
            val str = connect().input.readln()
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
}

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