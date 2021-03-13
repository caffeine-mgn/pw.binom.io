package pw.binom.io.httpClient

import pw.binom.URI
import pw.binom.charset.Charsets
import pw.binom.io.IOException
import pw.binom.io.Sha1MessageDigest
import pw.binom.io.http.*
import pw.binom.io.http.websocket.HandshakeSecret
import pw.binom.io.http.websocket.InvalidSecurityKeyException
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpClient.websocket.ClientWebSocketConnection
import pw.binom.io.use

class DefaultHttpRequest(
    var method: HTTPMethod,
    val URI: URI,
    val client: HttpClient,
    val channel: AsyncAsciiChannel,
) : HttpRequest {
    private var closed = false
    override val headers = HashHeaders()

    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("HttpRequest already closed")
        }
    }

    init {
        val port = URI.port
        val host = if (port == null) {
            URI.host
        } else {
            "${URI.host}:$port"
        }
        if (client.useKeepAlive) {
            headers[Headers.CONNECTION] = Headers.KEEP_ALIVE
        }
        headers[Headers.HOST] = host
        headers[Headers.ACCEPT_ENCODING] = "gzip, deflate, identity"
        headers[Headers.ACCEPT] = "*/*"
    }

    private suspend fun sendHeaders() {
        channel.writer.append(method.code).append(" ").append(URI.request).append(" ").append("HTTP/1.1\r\n")
        headers.forEachHeader { key, value ->
            channel.writer.append(key).append(": ").append(value).append("\r\n")
        }
        channel.writer.append("\r\n")
    }

    private val keepAlive
        get() = headers.keepAlive

    override suspend fun writeData(): AsyncHttpRequestOutput {
        checkClosed()
        if (headers.contentLength == null) {
            headers.transferEncoding = Headers.CHUNKED
        }
        sendHeaders()
        val encode = headers.transferEncoding
        if (encode != null) {
            when (encode.toLowerCase()) {
                Headers.CHUNKED.toLowerCase() -> {
                    closed = true
                    return RequestAsyncChunkedOutput(
                        URI = URI,
                        client = client,
                        keepAlive = keepAlive,
                        channel = channel,
                    )
                }
                else -> throw IOException("Unknown Transfer Encoding \"$encode\"")
            }
        }
        val len = headers.contentLength
        if (len != null) {
            closed = true
            return RequestAsyncContentLengthOutput(
                URI = URI,
                client = client,
                keepAlive = keepAlive,
                channel = channel,
                contentLength = len
            )
        }
        throw IllegalStateException("Unknown type of Transfer Encoding")
    }

    override suspend fun writeData(func: suspend (AsyncHttpRequestOutput) -> Unit): HttpResponse {
        val data = writeData()
        try {
            func(data)
            data.flush()
            return data.getResponse()
        } catch (e: Throwable) {
            runCatching { data.asyncClose() }
            throw e
        }
    }

    override suspend fun writeText(): AsyncHttpRequestWriter {
        val dataChannel = writeData()
        return RequestAsyncHttpRequestWriter(
            output = dataChannel,
            charset = headers.charset?.let { Charsets.get(it) } ?: Charsets.UTF8
        )
    }

    override suspend fun writeText(func: suspend (AsyncHttpRequestWriter) -> Unit): HttpResponse {
        val data = writeText()
        try {
            func(data)
            data.flush()
            return data.getResponse()
        } catch (e: Throwable) {
            runCatching { data.asyncClose() }
            throw e
        }
    }

    override suspend fun getResponse(): HttpResponse {
        checkClosed()
        val len = headers.contentLength
        if (len != null) {
            throw IllegalStateException("Can't get Response. Header contains \"${Headers.CONTENT_LENGTH}: $len\". Expected request data $len bytes")
        }
        val encode = headers.transferEncoding
        if (encode != null) {
            throw IllegalStateException("Can't get Response. Header contains \"${Headers.TRANSFER_ENCODING}: $encode\". Expected request data")
        }
        sendHeaders()
        channel.writer.flush()
        return DefaultHttpResponse.read(
            URI = URI,
            client = client,
            keepAlive = keepAlive,
            channel = channel,
        )
    }


    override suspend fun startWebSocket(origin: String?): WebSocketConnection {
        headers[Headers.CONNECTION] = Headers.UPGRADE
        headers[Headers.UPGRADE] = Headers.WEBSOCKET
        headers[Headers.SEC_WEBSOCKET_VERSION] = "13"
        headers[Headers.ORIGIN] = origin

        val requestKey = HandshakeSecret.generateRequestKey()
        val responseKey = HandshakeSecret.generateResponse(Sha1MessageDigest(), requestKey)
        headers[Headers.SEC_WEBSOCKET_KEY] = requestKey
        val resp = getResponse()
        val respKey = resp.headers.getSingle(Headers.SEC_WEBSOCKET_ACCEPT)
            ?: throw IOException("Invalid Server Response. Missing header \"${Headers.SEC_WEBSOCKET_ACCEPT}\"")
        if (respKey != responseKey) {
            throw InvalidSecurityKeyException()
        }

        return ClientWebSocketConnection(
            input = channel.reader,
            output = channel.writer,
            rawConnection = channel.channel
        )
    }

    override suspend fun asyncClose() {
        checkClosed()
        channel.asyncClose()
    }
}