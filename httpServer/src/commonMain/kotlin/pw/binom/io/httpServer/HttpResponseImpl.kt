package pw.binom.io.httpServer

import pw.binom.io.AsyncOutputStream
import pw.binom.io.socket.ConnectionManager
import pw.binom.io.writeln

class HttpResponseImpl(
        status: Int,
        headers: Map<String, List<String>>,
        headerSendded: Boolean,
        private val connection: ConnectionManager.Connection,
        private val request: HttpRequest
) : HttpResponse {
    private val _header = HashMap<String, ArrayList<String>>()

    init {
        headers.forEach { k ->
            _header[k.key] = ArrayList(k.value)
        }
    }

    override val headers: Map<String, List<String>>
        get() = _header

    override var status: Int = status

    private var headerResp = headerSendded

    internal suspend fun endResponse() {
        if (sendded == 0L && !headers.containsKey("Content-Length")) {
            addHeader("Content-Length", "0")
        }
        sendHeader()
    }

    private var sendded = 0L

    private suspend fun sendHeader() {
        if (headerResp)
            return
        headerResp = true
        connection.output.writeln("HTTP/1.1 $status OK")
        _header.forEach { h ->
            h.value.forEach {
                connection.output.writeln("${h.key}: $it")
            }
        }



        if (!headers.containsKey("Connection")) {
            if (headers["Content-Length"]?.singleOrNull()?.toLongOrNull() != null) {
                addHeader("Connection", "keep-alive")
                connection.output.writeln("Connection: keep-alive")
            } else {
                addHeader("Connection", "close")
                connection.output.writeln("Connection: close")
            }
        }

        connection.output.writeln("")
    }

    override val output = object : AsyncOutputStream {
        override fun close() {
        }

        override suspend fun flush() {
        }

        override suspend fun write(data: ByteArray, offset: Int, length: Int): Int {
            sendHeader()
            val w = connection.output.write(data, offset, length)
            sendded += w
            return w
        }

    }

    override fun addHeader(name: String, value: String) {
        _header.getOrPut(name) { ArrayList() }.add(value)
    }

    override fun resetHeader(name: String, value: String) {
        _header.getOrPut(name) { ArrayList() }.clear()
        addHeader(name, value)
    }

    internal var disconnectFlag = false
    internal var detachFlag = false

    override fun disconnect() {
        disconnectFlag = true
    }

    override fun detach(): HttpConnectionState {
        detachFlag = true
        return HttpConnectionState(
                status = status,
                responseHeaders = headers,
                channel = connection.detach(),
                headerSendded = headerResp,
                uri = request.uri,
                method = request.method,
                requestHeaders = request.headers
        )
    }

    init {
        if (!headers.containsKey("Server"))
            resetHeader("Server", "Binom Server")
        if (!headers.containsKey("Content-Type"))
            resetHeader("Content-Type", "text/html; charset=utf-8")
    }
}