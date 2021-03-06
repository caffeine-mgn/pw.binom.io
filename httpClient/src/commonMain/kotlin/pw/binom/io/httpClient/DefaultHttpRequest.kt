package pw.binom.io.httpClient

import pw.binom.URL
import pw.binom.base64.Base64
import pw.binom.charset.Charsets
import pw.binom.io.IOException
import pw.binom.io.http.*
import pw.binom.io.http.websocket.WebSocketConnection

class DefaultHttpRequest(
    var method: HTTPMethod,
    val url: URL,
    val client: HttpClient,
    val channel: AsyncAsciiChannel,
) : HttpRequest {
    private var closed = false
    override val headers = HashHeaders()

    fun use(auth: BasicAuth) {
        headers[Headers.AUTHORIZATION] = "Basic ${Base64.encode("${auth.login}:${auth.password}".encodeToByteArray())}"
    }

    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("HttpRequest already closed")
        }
    }

    init {
        val port = url.port
        val host = if (port == null) {
            url.host
        } else {
            "${url.host}:$port"
        }
        if (client.useKeepAlive) {
            headers[Headers.CONNECTION] = Headers.KEEP_ALIVE
        }
        headers[Headers.HOST] = host
        headers[Headers.ACCEPT_ENCODING] = "gzip, deflate, identity"
        headers[Headers.ACCEPT] = "*/*"
    }

    private suspend fun sendHeaders() {
        channel.writer.append(method.name).append(" ").append(url.request).append(" ").append("HTTP/1.1\r\n")
        headers.forEachHeader { key, value ->
            channel.writer.append(key).append(": ").append(value).append("\r\n")
        }
        channel.writer.append("\r\n")
    }

    private val keepAlive
        get() = headers[Headers.CONNECTION]
            ?.firstOrNull()
            .equals(Headers.KEEP_ALIVE, ignoreCase = true)

    override suspend fun writeData(): AsyncHttpRequestOutput {
        checkClosed()
        sendHeaders()
        val encode = headers.getTransferEncoding()
        if (encode != null) {
            when (encode.toLowerCase()) {
                Headers.CHUNKED.toLowerCase() -> {
                    closed = true
                    RequestAsyncChunkedOutput(
                        url = url,
                        client = client,
                        keepAlive = keepAlive,
                        channel = channel,
                    )
                }
                else -> throw IOException("Unknown Transfer Encoding \"$encode\"")
            }
        }
        val len = headers.getContentLength()
        if (len != null) {
            closed = true
            return RequestAsyncContentLengthOutput(
                url = url,
                client = client,
                keepAlive = keepAlive,
                channel = channel,
                contentLength = len
            )
        }

        headers[Headers.TRANSFER_ENCODING] = Headers.CHUNKED
        closed = true
        return RequestAsyncChunkedOutput(
            url = url,
            client = client,
            keepAlive = keepAlive,
            channel = channel,
        )
    }

    override suspend fun writeText(): AsyncHttpRequestWriter {
        val dataChannel = writeData()
        return RequestAsyncHttpRequestWriter(
            output = dataChannel,
            charset = headers.getCharset() ?: Charsets.UTF8
        )
    }

    override suspend fun getResponse(): HttpResponse {
        checkClosed()
        val len = headers.getContentLength()
        if (len != null) {
            throw IllegalStateException("Can't get Response. Header contains \"${Headers.CONTENT_LENGTH}: $len\". Expected request data $len bytes")
        }
        val encode = headers.getTransferEncoding()
        if (encode != null) {
            throw IllegalStateException("Can't get Response. Header contains \"${Headers.TRANSFER_ENCODING}: $encode\". Expected request data")
        }
        sendHeaders()
        channel.writer.flush()
        return DefaultHttpResponse.read(
            url = url,
            client = client,
            keepAlive = keepAlive,
            channel = channel,
        )
    }


    override suspend fun startWebSocket(): WebSocketConnection {
        TODO("Not yet implemented")
    }

    override suspend fun asyncClose() {
        checkClosed()
        channel.asyncClose()
    }
}