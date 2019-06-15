package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.URL
import pw.binom.io.*
import pw.binom.io.socket.ConnectionManager
import pw.binom.io.socket.SocketChannel
import pw.binom.io.socket.SocketClosedException

class AsyncHttpClient(val connectionManager: ConnectionManager) : Closeable {
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
            "http" -> UrlConnectHTTP(method, url, this)
            else -> throw RuntimeException("Unknown protocol \"${url.protocol}\"")
        }
        r.addRequestHeader("User-Agent", "Binom Client")
        r.addRequestHeader("Connection", "keep-alive")
        r.addRequestHeader("Host", url.host)
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

    private suspend fun skipInput() {
        while (true) {
            val buf = ByteArray(DEFAULT_BUFFER_SIZE)
            if (inputStream.read(buf) <= 0)
                break
        }
    }

    override suspend fun close() {
        if (closed)
            return
        skipInput()
        closed = true

        val channel = socket?.detach()
        socket = null

        if (channel?.isConnected == true) {
            if (connectionKeepAlive) {
                client.pushConnection(url.protocol, url.host, url.port ?: url.defaultPort!!, channel)
            } else {
                channel.close()
            }
        }
    }

    override val inputStream: AsyncInputStream
        get() = _inputStream

    override val outputStream: AsyncOutputStream
        get() = _outputStream


    override suspend fun responseCode(): Int {
        readResponse()
        return _responseCode
    }

    private var eof = false

    private inner class RawOutputStream : AsyncOutputStream {
        override suspend fun write(data: ByteArray, offset: Int, length: Int): Int {
            sendRequest()
            return connect().output.write(data, offset, length)
        }

        override suspend fun flush() {
            connect().output.flush()
        }

        override suspend fun close() {
        }

    }

    private inner class RawInputStream : AsyncInputStream {
        private var readed = 0uL
        private var chunkedSize: ULong? = null
        override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
            try {
                if (closed || eof)
                    return 0
                readResponse()
                if (chunked) {
                    while (true) {
                        if (chunkedSize == null) {
                            val chunkedSize = connect().input.readln()
                            this.chunkedSize = chunkedSize.toULongOrNull(16)
                                    ?: throw RuntimeException("Invalid Chunked Size: \"${chunkedSize}\"")
                            this.chunkedSize = this.chunkedSize!!
                            readed = 0uL
                        }

                        if (chunkedSize == 0uL) {
                            if (
                                    connect().input.read() != 13.toByte()
                                    || connect().input.read() != 10.toByte()
                            )
                                throw IOException("Invalid end body")
                            eof = true
                            close()
                            return 0
                        }
                        if (chunkedSize!! - readed <= 0uL) {
                            chunkedSize = null
                            if (
                                    connect().input.read() != 13.toByte()
                                    || connect().input.read() != 10.toByte()
                            )
                                throw IOException("Invalid end of chunk")
                            continue
                        }

                        val r = minOf(chunkedSize!! - readed, length.toULong())
                        val b = connect().input.read(data, offset, r.toInt())
                        readed += b.toULong()
                        return b
                    }
                } else {


                    if (_contentLength > 0uL && (_contentLength - readed <= 0uL))
                        return 0
                    val r = if (_contentLength > 0uL && (_contentLength - readed < length.toULong())) {
                        connect().input.read(data, offset, (_contentLength - readed).toInt())
                    } else
                        connect().input.read(data, offset, length)
                    readed += r.toULong()
                    if (r < length || (_contentLength > 0uL && readed == _contentLength)) {
                        this@UrlConnectHTTP.close()
                    }
                    return r
                }
            } catch (e: SocketClosedException) {
                eof = true
                return 0
            }
        }

        override suspend fun close() {
        }

    }

    private val _inputStream = RawInputStream()
    private val _outputStream = RawOutputStream()

    private var requestSend = false
    private var responseRead = false
    private var closed = false
    private var _responseCode = 0
    private var _contentLength = 0uL
    private var chunked = false
    private var connectionKeepAlive = false

    private var requestHeaders = HashMap<String, ArrayList<String>>()
    private var responseHeaders = HashMap<String, ArrayList<String>>()

    override fun addRequestHeader(key: String, value: String) {
        if (requestSend)
            throw IllegalStateException("Headers already sended")
        requestHeaders.getOrPut(key) { ArrayList() }.add(value)
    }

    private suspend fun sendRequest() {
        if (requestSend) {
            return
        }

        connect().output.write("$method ${url.uri} HTTP/1.1\r\n")
        requestHeaders.forEach { en ->
            en.value.forEach {
                connect().output.write(en.key)
                connect().output.write(": ")
                connect().output.write(it)
                connect().output.write("\r\n")
            }
        }
        connect().output.write("\r\n")
        requestSend = true
    }

    private var socket: ConnectionManager.Connection? = null


    private suspend fun readResponse() {
        sendRequest()
        if (responseRead)
            return


        val responseLine = connect().input.readln()
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
            if (items[0] == "Content-Length")
                _contentLength = items[1].toULong()
            if (items[0] == "Transfer-Encoding" && items[1] == "chunked")
                chunked = true

            responseHeaders.getOrPut(items[0]) { ArrayList() }.add(items[1])
        }
        connectionKeepAlive = responseHeaders["Connection"]?.firstOrNull() == "keep-alive"
        responseRead = true
    }

    private fun connect(): ConnectionManager.Connection {
        if (closed)
            throw IllegalStateException("Connection already closed")

        if (socket == null) {
            val channel = client.pollConnection(url.protocol, url.host, url.port ?: url.defaultPort!!)
            if (channel != null) {
                socket = client.connectionManager.attach(channel = channel, attachment = this)
            }
        }

        if (socket == null) {
            socket = client.connectionManager.connect(
                    host = url.host,
                    port = url.port ?: url.defaultPort!!,
                    attachment = this
            )
        }
        return socket!!
    }

    override suspend fun getResponseHeaders(): Map<String, List<String>> {
        readResponse()
        return responseHeaders
    }
}