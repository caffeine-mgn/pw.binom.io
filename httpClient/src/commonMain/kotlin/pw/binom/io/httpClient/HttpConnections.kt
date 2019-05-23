package pw.binom.io.httpClient

import pw.binom.URL
import pw.binom.io.*
import pw.binom.io.socket.Socket
import pw.binom.io.zip.InflateInputStream

@ExperimentalUnsignedTypes
class HttpConnections(val allowKeepAlive: Boolean = true) : Closeable {
    private val connections = HashMap<String, Socket>()
    override fun close() {
        connections.values.forEach {
            if (it.connected)
                it.close()
        }
        connections.clear()
    }

    internal fun pollConnection(host: String, port: Int): Socket? {
        if (!allowKeepAlive)
            return null
        val g = connections["$host:$port"]
        if (g != null) {
            if (!g.connected) {
                connections.remove("$host:$port")
                return null
            }
        }
        return g
    }

    internal fun pushConnect(host: String, port: Int, socket: Socket) {
        if (!allowKeepAlive) {
            if (socket.connected)
                socket.close()
            return
        }
        if (socket.connected) {
            connections["$host:$port"]?.close()
            connections["$host:$port"] = socket
        }
    }

    fun request(url: URL, method: String = "GET"): URLRequest = when (url.protocol) {
        "http" -> HttpURLRequestImpl(method, url, this)
        else -> throw IOException("Unsupported protocol ${url.protocol}")
    }


    interface URLRequest : Closeable {
        val responseCode: Int
        val contentLength: ULong
        fun addRequestHeader(key: String, value: String)
        fun getResponseHeader(key: String): List<String>?
        val responseHeaderNames: Collection<String>
        val inputStream: InputStream
    }
}

@ExperimentalUnsignedTypes
private class HttpURLRequestImpl(val method: String, val url: URL, private val connections: HttpConnections) : HttpConnections.URLRequest {

    private inner class RawHttpInputStream : InputStream {
        override fun read(data: ByteArray, offset: Int, length: Int): Int {
            if (closed)
                return 0
            readResponse()
            val r = if (contentLength > 0uL && (contentLength - readed < length.toULong())) {
                connect().read(data, offset, (contentLength - readed).toInt())
            } else
                connect().read(data, offset, length)
            readed += r.toULong()
            if (r < length || (contentLength > 0uL && readed == contentLength)) {
                this@HttpURLRequestImpl.close()
            }
            return r
        }

        override fun close() {
        }
    }

    private val rawInputStream = RawHttpInputStream()

    private var _inputStream: InputStream? = null

    override val inputStream: InputStream
        get() {
            if (_inputStream != null)
                return _inputStream!!

            val encode = getResponseHeader("Content-Encoding")?.firstOrNull()
            _inputStream = when (encode) {
                "deflate" -> InflateInputStream(rawInputStream, wrap = false)
                null -> rawInputStream
                else -> throw RuntimeException("Unknown response encode \"$encode\"")
            }
            return _inputStream!!
        }

    private var readed = 0uL
    private var closed = false
    private var connectionKeepAlive = false

    override fun close() {
        if (closed)
            return
        closed = true
        if (socket?.connected == true) {
            if (connectionKeepAlive) {
                connections.pushConnect(url.host, url.port ?: url.defaultPort, socket!!)
            } else {
                socket!!.close()
            }
        }
    }

    override val responseHeaderNames: Collection<String>
        get() {
            readResponse()
            return responseHeaders.keys
        }

    override fun getResponseHeader(key: String): List<String>? {
        readResponse()
        return responseHeaders[key]
    }

    override val contentLength: ULong
        get() {
            readResponse()
            return _contentLength
        }

    private var socket: Socket? = null
    private var requestSend = false
    private var responseRead = false
    private var requestHeaders = HashMap<String, ArrayList<String>>()
    private var responseHeaders = HashMap<String, ArrayList<String>>()
    private var _contentLength = 0uL

    init {
        addRequestHeader("Host", url.host)
        addRequestHeader("User-Agent", "Binom-Client")
        if (connectionKeepAlive)
            addRequestHeader("Connection", "keep-alive")
        else
            addRequestHeader("Connection", "close")
//        addRequestHeader("Accept-Encoding", "gzip, deflate, br")
        addRequestHeader("Accept-Encoding", "deflate")
    }

    override fun addRequestHeader(key: String, value: String) {
        if (requestSend)
            throw IllegalStateException("Headers already sended")
        requestHeaders.getOrPut(key) { ArrayList() }.add(value)
    }

    private fun connect(): Socket {
        if (closed)
            throw IllegalStateException("Connection already closed")
        if (socket == null) {
            socket = connections.pollConnection(url.host, url.port ?: url.defaultPort)
        }
        if (socket == null) {
            socket = Socket()
            socket!!.connect(url.host, url.port ?: url.defaultPort)
        }
        return socket!!
    }

    private fun sendRequest() {
        if (requestSend) {
            return
        }

        connect().write("$method ${url.uri} HTTP/1.1\r\n")
        requestHeaders.forEach { en ->
            en.value.forEach {
                connect().write(en.key)
                connect().write(": ")
                connect().write(it)
                connect().write("\r\n")
            }
        }
        connect().write("\r\n")
        requestSend = true
    }

    fun getHeader(key: String): List<String>? {
        readResponse()
        return responseHeaders[key]
    }

    private var _responseCode = 0

    override val responseCode: Int
        get() {
            readResponse()
            return _responseCode
        }


    private fun readResponse() {
        sendRequest()
        if (responseRead)
            return

        _responseCode = connect().readLn().splitToSequence(' ').iterator().let {
            it.next()
            it.next()
        }.toInt()
        while (true) {
            val str = connect().readLn()
            if (str.isEmpty()) {
                break
            }

            val items = str.split(": ")
            if (items[0] == "Content-Length")
                _contentLength = items[1].toULong()

            if (items[0] == "Connection" && items[1] == "keep-alive")
                connectionKeepAlive = true
            if (items[0] == "Connection" && items[1] == "close")
                connectionKeepAlive = false
            responseHeaders.getOrPut(items[0]) { ArrayList() }.add(items[1])
        }
        responseRead = true
    }

}