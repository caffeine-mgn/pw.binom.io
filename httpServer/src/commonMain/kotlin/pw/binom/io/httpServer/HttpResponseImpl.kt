package pw.binom.io.httpServer

import pw.binom.io.OutputStream
import pw.binom.io.socket.ConnectionManager
import pw.binom.io.socket.SocketChannel
import pw.binom.io.writeln

class HttpResponseImpl(val connection: ConnectionManager.Connection) : HttpResponse {
    private val _header = HashMap<String, ArrayList<String>>()
    override val headers: Map<String, List<String>>
        get() = _header
    override var status: Int = 404

    private var headerResp = false

    internal fun endResponse() {
        if (sendded == 0L && !headers.containsKey("Content-Length")) {
            addHeader("Content-Length", "0")
        }
        sendHeader()
    }

    private var sendded = 0L

    private fun sendHeader() {
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

    override val output = object : OutputStream {
        override fun close() {
        }

        override fun flush() {
        }

        override fun write(data: ByteArray, offset: Int, length: Int): Int {
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

    override fun detach(): SocketChannel {
        detachFlag = true
        return connection.detach()
    }

    init {
        resetHeader("Server", "Binom Server")
        resetHeader("Content-Type", "text/html; charset=utf-8")
    }
}