package pw.binom.io.httpClient

import pw.binom.BINOM_VERSION
import pw.binom.Environment
import pw.binom.charset.Charsets
import pw.binom.crypto.Sha1MessageDigest
import pw.binom.io.AsyncChannel
import pw.binom.io.IOException
import pw.binom.io.http.*
import pw.binom.io.http.websocket.HandshakeSecret
import pw.binom.io.http.websocket.InvalidSecurityKeyException
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.os
import pw.binom.url.URL

private val defaultUserAgent =
    "binom-$BINOM_VERSION/kotlin-${KotlinVersion.CURRENT} os/${Environment.os.name.lowercase()}"

class DefaultHttpRequest constructor(
    override var method: String,
    override val uri: URL,
    val client: BaseHttpClient,
    val channel: AsyncAsciiChannel,
) : HttpRequest {
    private var closed = false
    override val headers = HashHeaders()

    init {
        HttpMetrics.defaultHttpRequestCountMetric.inc()
    }

    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("HttpRequest already closed")
        }
    }

    init {
        val port = uri.port
        val host = if (port == null) {
            uri.host
        } else {
            "${uri.host}:$port"
        }
        if (client.useKeepAlive) {
            headers[Headers.CONNECTION] = Headers.KEEP_ALIVE
        }
        headers[Headers.HOST] = host
        headers[Headers.ACCEPT_ENCODING] = "gzip, deflate, identity"
        headers[Headers.ACCEPT] = "*/*"
        headers[Headers.USER_AGENT] = defaultUserAgent
    }

    private suspend fun sendHeaders() {
        val request = if (uri.request.isEmpty()) {
            "/"
        } else {
            uri.request
        }
        channel.writer.append(method).append(" ").append(request).append(" ").append("HTTP/1.1${Utils.CRLF}")
        headers.forEachHeader { key, value ->
            channel.writer.append(key).append(": ").append(value).append(Utils.CRLF)
        }
        channel.writer.append(Utils.CRLF)
    }

    private val keepAlive
        get() = headers.keepAlive

    override suspend fun writeData(): AsyncHttpRequestOutput {
        checkClosed()
        if (headers.contentLength == null) {
            headers.transferEncoding = Encoding.CHUNKED
        }
        sendHeaders()
        val encode = headers.transferEncoding
        if (encode != null) {
            when (encode.lowercase()) {
                Encoding.CHUNKED -> {
                    closed = true
                    HttpMetrics.defaultHttpRequestCountMetric.dec()
                    return RequestAsyncChunkedOutput(
                        URI = uri,
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
            HttpMetrics.defaultHttpRequestCountMetric.dec()
            return RequestAsyncContentLengthOutput(
                URI = uri,
                client = client,
                keepAlive = keepAlive,
                channel = channel,
                contentLength = len,
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
            try {
                data.asyncClose()
            } catch (ex: Throwable) {
                ex.addSuppressed(e)
                throw e
            }
            throw e
        }
    }

    override suspend fun writeText(): AsyncHttpRequestWriter {
        val dataChannel = writeData()
        return RequestAsyncHttpRequestWriter(
            output = dataChannel,
            bufferSize = client.bufferSize,
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
        if (len != null && len > 0uL) {
            throw IllegalStateException("Can't get Response. Header contains \"${Headers.CONTENT_LENGTH}: $len\". Expected request data $len bytes")
        }
        val encode = headers.transferEncoding
        if (encode != null) {
            throw IllegalStateException("Can't get Response. Header contains \"${Headers.TRANSFER_ENCODING}: $encode\". Expected request data")
        }
        sendHeaders()
        channel.writer.flush()
        closed = true
        HttpMetrics.defaultHttpRequestCountMetric.dec()
        val v = DefaultHttpResponse.read(
            uri = uri,
            client = client,
            keepAlive = keepAlive,
            channel = channel,
        )
        return v
    }

    override suspend fun startWebSocket(origin: String?, masking: Boolean): WebSocketConnection {
        headers[Headers.CONNECTION] = Headers.UPGRADE
        headers[Headers.UPGRADE] = Headers.WEBSOCKET
        headers[Headers.SEC_WEBSOCKET_VERSION] = "13"
        headers[Headers.ORIGIN] = origin

        val requestKey = HandshakeSecret.generateRequestKey()
        val responseKey = HandshakeSecret.generateResponse(Sha1MessageDigest(), requestKey)
        headers[Headers.SEC_WEBSOCKET_KEY] = requestKey
        val resp = getResponse()
        if (resp.responseCode != 101) {
            throw IOException("Invalid Response code: ${resp.responseCode}")
        }
        val respKey = resp.headers.getSingleOrNull(Headers.SEC_WEBSOCKET_ACCEPT)
            ?: throw IOException("Invalid Server Response. Missing header \"${Headers.SEC_WEBSOCKET_ACCEPT}\"")
        if (respKey != responseKey) {
            throw InvalidSecurityKeyException()
        }
        HttpMetrics.defaultHttpRequestCountMetric.dec()
        return client.webSocketConnectionPool.new(
            input = channel.reader,
            output = channel.writer,
            masking = masking,
        )
//        return WebSocketConnectionImpl(
//            input = channel.reader,
//            output = channel.writer,
//            masking = masking,
//            messagePool = client.messagePool,
//        )
    }

    override suspend fun startTcp(): AsyncChannel {
        headers[Headers.CONNECTION] = Headers.UPGRADE
        headers[Headers.UPGRADE] = Headers.TCP
        val resp = getResponse()
        if (resp.responseCode != 101) {
            throw IOException("Invalid Response code: ${resp.responseCode}")
        }
        HttpMetrics.defaultHttpRequestCountMetric.dec()
        return channel.channel
    }

    override suspend fun asyncClose() {
        if (closed) {
            return
        }
        try {
            channel.asyncClose()
            closed = true
        } finally {
            HttpMetrics.defaultHttpRequestCountMetric.dec()
        }
    }
}
