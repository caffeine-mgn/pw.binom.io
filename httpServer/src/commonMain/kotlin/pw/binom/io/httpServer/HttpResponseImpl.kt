package pw.binom.io.httpServer

import pw.binom.io.AsyncOutputStream
import pw.binom.io.LazyAsyncOutputStream
import pw.binom.io.http.AsyncChunkedOutputStream
import pw.binom.io.http.AsyncContentLengthOututStream
import pw.binom.io.http.Headers
import pw.binom.io.socket.ConnectionManager
import pw.binom.io.writeln

private fun statusToText(code: Int) =
        when (code) {
            100 -> "Continue"
            101 -> "Switching Protocols"
            102 -> "Processing"
            200 -> "OK"
            201 -> "Created"
            202 -> "Accepted"
            203 -> "Non-Authoritative Information"
            204 -> "No Content"
            205 -> "Reset Content"
            206 -> "Partial Content"
            207 -> "Multi-Status"
            208 -> "Already Reported"
            300 -> "Multiple Choices"
            301 -> "Moved Permanently"
            302 -> "Found"
            303 -> "See Other"
            304 -> "Not Modified"
            305 -> "Use Proxy"
            307 -> "Temporary Redirect"
            308 -> "Permanent Redirect"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            402 -> "Payment Required"
            403 -> "Forbidden"
            404 -> "Not Found"
            405 -> "Method Not Allowed"
            406 -> "Not Acceptable"
            407 -> "Proxy Authentication Required"
            408 -> "Request Timeout"
            409 -> "Conflict"
            410 -> "Gone"
            411 -> "Length Required"
            412 -> "Precondition Failed"
            413 -> "Payload Too Large"
            414 -> "URI Too Long"
            415 -> "Unsupported Media Type"
            416 -> "Range Not Satisfiable"
            417 -> "Expectation Failed"
            418 -> "I'm a teapot"
            419 -> "Authentication Timeout"
            421 -> "Misdirected Request"
            422 -> "Unprocessable Entity"
            423 -> "Locked"
            424 -> "Failed Dependency"
            426 -> "Upgrade Required"
            428 -> "Precondition Required"
            429 -> "Too Many Requests"
            431 -> "Request Header Fields Too Large"
            434 -> "Requested host unavailable"
            449 -> "Retry With"
            451 -> "Unavailable For Legal Reasons"
            499 -> "Client Closed Request"
            500 -> "Internal Server Error"
            501 -> "Not Implemented"
            502 -> "Bad Gateway"
            503 -> "Service Unavailable"
            504 -> "Gateway Timeout"
            505 -> "HTTP Version Not Supported"
            506 -> "Variant Also Negotiates"
            507 -> "Insufficient Storage"
            509 -> "Bandwidth Limit Exceeded"
            510 -> "Not Extended"
            511 -> "Network Authentication Required"
            520 -> "Unknown Error"
            521 -> "Web Server Is Down"
            522 -> "Connection Timed Out"
            523 -> "Origin Is Unreachable"
            524 -> "A Timeout Occurred"
            525 -> "SSL Handshake Failed"
            526 -> "Invalid SSL Certificate"
            else -> "Unknown Status"
        }

class HttpResponseImpl(
        val keepAlive: Boolean,
        status: Int,
        headers: Map<String, List<String>>,
        headerSendded: Boolean,
        private val connection: ConnectionManager.Connection,
        private val request: HttpRequest
) : HttpResponse {
    override fun clearHeaders() {
        _header.clear()
    }

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

        if (!_header.containsKey(Headers.CONNECTION)) {
            addHeader(Headers.CONNECTION, Headers.KEEP_ALIVE)
        }

        if (!_header.containsKey(Headers.CONTENT_LENGTH)){
            addHeader(Headers.TRANSFER_ENCODING,Headers.CHUNKED)
        }

        connection.output.writeln("HTTP/1.1 $status ${statusToText(status)}")
        _header.forEach { h ->
            h.value.forEach {
                connection.output.writeln("${h.key}: $it")
            }
        }

/*

        if (!headers.containsKey(Headers.CONNECTION)) {
            val contentLengthDefine = headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.toLongOrNull() != null
            if (keepAlive && contentLengthDefine) {
                addHeader(Headers.CONNECTION, Headers.KEEP_ALIVE)
                connection.output.writeln("${Headers.CONNECTION}: ${Headers.KEEP_ALIVE}")
            } else {
                addHeader(Headers.CONNECTION, Headers.CLOSE)
                connection.output.writeln("${Headers.CONNECTION}: ${Headers.CLOSE}")
            }
        }
*/
        connection.output.writeln("")
    }

    override val output =
            LazyAsyncOutputStream {
                sendHeader()
                when {
                    this.headers[Headers.TRANSFER_ENCODING]?.any { it == Headers.CHUNKED } == true ->
                        AsyncChunkedOutputStream(connection.output)
                    this.headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull() != null ->
                        AsyncContentLengthOututStream(connection.output, this.headers[Headers.CONTENT_LENGTH]!!.single().toULong())
                    else -> throw RuntimeException("Unknown Output Stream Type")
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
        if (!headers.containsKey(Headers.SERVER))
            resetHeader(Headers.SERVER, "Binom Server")
        if (!headers.containsKey(Headers.CONTENT_TYPE))
            resetHeader(Headers.CONTENT_TYPE, "text/html; charset=utf-8")
    }
}

private class PrivateHttpResponse(
        override var status: Int,
        override val output: AsyncOutputStream,
        headers: Map<String, List<String>>,
        val parent: HttpResponse) : HttpResponse {
    override fun clearHeaders() {
        _headers.clear()
    }

    private val _headers = HashMap<String, ArrayList<String>>()

    init {
        headers.forEach { k ->
            _headers[k.key] = ArrayList(k.value)
        }
    }

    override val headers: Map<String, List<String>>
        get() = _headers

    override fun resetHeader(name: String, value: String) {
        _headers.remove(name)
        addHeader(name, value)
    }

    override fun addHeader(name: String, value: String) {
        _headers.getOrPut(name) { ArrayList() }.add(value)
    }

    override fun detach(): HttpConnectionState = parent.detach()

    override fun disconnect() = parent.disconnect()

}

fun HttpResponse.withOutput(stream: AsyncOutputStream): HttpResponse =
        PrivateHttpResponse(
                status = status,
                headers = headers,
                output = stream,
                parent = this
        )

fun HttpResponse.addHeaders(resp: HttpResponse) {
    resp.headers.forEach { k ->
        k.value.forEach {
            addHeader(k.key, it)
        }
    }
}

fun HttpResponse.resetHeaders(resp: HttpResponse) {
    clearHeaders()
    addHeaders(resp)
}