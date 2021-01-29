package pw.binom.io.httpClient

import pw.binom.*
import pw.binom.base64.Base64EncodeOutput
import pw.binom.io.*
import pw.binom.io.http.Headers
import pw.binom.io.http.websocket.HandshakeSecret
import pw.binom.io.http.websocket.InvalidSecurityKeyException
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpClient.websocket.ClientWebSocketConnection
import kotlin.random.Random
import kotlin.time.ExperimentalTime

internal class UrlConnectImpl(
        val method: String,
        val url: URL,
        val client: AsyncHttpClient,
        val outputFlushSize: Int) : AsyncHttpClient.UrlConnect {
    override val headers: MutableMap<String, MutableList<String>> = HashMap()
    private var requestSent = false
    private var clientDataLength: ULong? = null
    private var chanked = false
    private var channel: AsyncHttpClient.Connection? = null

    init {
        headers[Headers.CONNECTION] = mutableListOf(Headers.KEEP_ALIVE)
        headers[Headers.HOST] = if (url.port == url.defaultPort)
            mutableListOf(url.host)
        else
            mutableListOf("${url.host}:${url.port}")
        headers[Headers.ACCEPT_ENCODING] = mutableListOf("gzip, deflate, identity")
        headers[Headers.ACCEPT] = mutableListOf("*/*")
    }

    private fun checkSent() {
        if (requestSent) {
            throw IllegalStateException("Request already sent")
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun sendRequest(withOutput: Boolean) {
        checkSent()
        requestSent = true
        if (withOutput) {
            clientDataLength = headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.let {
                it.toULongOrNull() ?: throw RuntimeException("Can't parse Content-Length \"$it\" to unsigned long")
            }
            if (clientDataLength == null && withOutput) {
                if (headers[Headers.TRANSFER_ENCODING]?.isEmpty() != false) {
                    headers.getOrPut(Headers.TRANSFER_ENCODING) { ArrayList() }.add(Headers.CHUNKED)
                    chanked = true
                }
            }
            if (!chanked) {
                chanked = clientDataLength == null && headers[Headers.TRANSFER_ENCODING]?.singleOrNull() == Headers.CHUNKED
            }
        }
        val connection = client.borrowConnection(url)
        val buffered = connection.channel.bufferedOutput(closeStream = false)
        val app = buffered.utf8Appendable()
        app.append("$method ${url.uri} HTTP/1.1\r\n")
        headers.forEach { en ->
            en.value.forEach {
                app.append(en.key).append(": ").append(it).append("\r\n")
            }
        }
        app.append("\r\n")
        buffered.flush()
        buffered.asyncClose()
        channel = connection
    }

    override suspend fun upload(): AsyncHttpClient.UrlRequest {
        sendRequest(true)
        val channel = channel!!
        return UrlRequestImpl(
                headers = headers,
                channel = channel,
                connection = this,
                flushBuffer = outputFlushSize,
                compressBufferSize = DEFAULT_BUFFER_SIZE,
                compressLevel = 6
        )
//        val chanked = clientDataLength == null && headers[Headers.TRANSFER_ENCODING]?.singleOrNull() == Headers.CHUNKED
//        if (!chanked && clientDataLength == null) {
//            throw IllegalStateException("Unknown Transfer Encoding")
//        }

        TODO()
//        return when {
//            else -> NoInputUrlRequest(connection)
//        }
    }

    @OptIn(ExperimentalTime::class)
    internal suspend fun compliteRequest(): UrlResponseImpl {
        val channel = channel!!
        val buffered = channel.channel.bufferedInput(closeStream = false)
        val reader = buffered.utf8Reader()
        val vv = reader.readln()
        val responseLine = vv
        if (responseLine == null) {
            channel.sslSession?.close()
            channel.channel.asyncClose()
            throw IOException("Invalid reponse line: Reponse is empty")
        }
        if (!responseLine.startsWith("HTTP/1.1 ") && !responseLine.startsWith("HTTP/1.0 "))
            throw IOException("Unsupported HTTP version. Response: \"$responseLine\"")
        val responseCode = responseLine.substring(9, 12).toInt()
        val responseHeaders = HashMap<String, ArrayList<String>>()
        while (true) {
            val str = reader.readln() ?: ""
            if (str.isEmpty()) {
                break
            }
            val items = str.split(": ")
            responseHeaders.getOrPut(items[0]) { ArrayList() }.add(items[1])
        }
        this.channel = null
        return UrlResponseImpl(
                responseCode = responseCode,
                headers = responseHeaders,
                channel = channel,
                client = client,
                url = url,
                input = buffered
        )
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun response(): AsyncHttpClient.UrlResponse {
        sendRequest(false)
        val channel = channel!!
        if (chanked) {
            ByteBuffer.alloc(5).use { buf ->
                buf.put('0'.toByte())
                buf.put('\r'.toByte())
                buf.put('\n'.toByte())
                buf.put('\r'.toByte())
                buf.put('\n'.toByte())
                buf.flip()
                channel.channel.write(buf)
            }
        }
        return compliteRequest()
    }

    override suspend fun websocket(origin: String?): WebSocketConnection {
        headers.remove(Headers.CONNECTION)
        addHeader(Headers.UPGRADE, Headers.WEBSOCKET)
        addHeader(Headers.CONNECTION, Headers.UPGRADE)
        addHeader(Headers.SEC_WEBSOCKET_VERSION, "13")
        if (origin != null)
            addHeader(Headers.ORIGIN, origin)

        val requestKey = StringBuilder().let { sb ->
            val buf = ByteBuffer.alloc(16)
            Random.nextBytes(buf)

            Base64EncodeOutput(sb).use {
                buf.clear()
                it.write(buf)
                buf.close()
            }
            sb.toString()
        }
        val responseKey = HandshakeSecret.generateResponse(Sha1(), requestKey)

        addHeader(Headers.SEC_WEBSOCKET_KEY, requestKey)
        sendRequest(false)
        val channel = this.channel!!
        val resp = compliteRequest()
        if (resp.headers[Headers.SEC_WEBSOCKET_ACCEPT]?.singleOrNull() != responseKey) {
            channel.asyncClose()
            throw InvalidSecurityKeyException()
        }
        return ClientWebSocketConnection(resp.input, channel.channel, channel.rawConnection)
    }

    override suspend fun asyncClose() {
        channel?.let {
            it.sslSession?.close()
            it.channel.asyncClose()
        }
    }

}