package pw.binom.io.httpServer

import pw.binom.*
import pw.binom.charset.Charsets
import pw.binom.compression.zlib.AsyncDeflaterOutput
import pw.binom.compression.zlib.AsyncGZIPOutput
import pw.binom.io.*
import pw.binom.io.http.*
import pw.binom.io.http.websocket.HandshakeSecret
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.httpServer.websocket.ServerWebSocketConnection

internal class HttpRequest2Impl(
    val channel: AsyncAsciiChannel,
    val server: HttpServer,
    override val method: String,
    override val headers: Headers,
    override val urn: URN
) : HttpRequest {
    companion object {
        suspend fun read(channel: AsyncAsciiChannel, server: HttpServer): HttpRequest2Impl {
            val request = channel.reader.readln()!!
            val items = request.split(' ', limit = 3)

            val headers = HashHeaders()
            while (true) {
                val s = channel.reader.readln() ?: break
                if (s.isEmpty()) {
                    break
                }
                val p = s.indexOf(':')
                if (p < 0) {
                    runCatching { channel.asyncClose() }
                    throw IOException("Invalid HTTP Header")
                }
                val headerKey = s.substring(0, p)
                val headerValue = s.substring(p + 2)
                headers.add(headerKey, headerValue)
            }

            return HttpRequest2Impl(
                urn = (items.getOrNull(1) ?: "").toURN,
                method = items[0],
                channel = channel,
                headers = headers,
                server = server,
            )
        }
    }

    private val closed = false
    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("Already closed")
        }
    }

    private var readInput: AsyncHttpInput? = null

    override fun readBinary(): AsyncInput {
        checkClosed()
        if (readInput != null) {
            throw IllegalStateException("Already reading")
        }
        if (!headers.bodyExist) {
            readInput = AsyncEmptyHttpInput
            return AsyncEmptyHttpInput
        }
        val len = headers.contentLength
        val encode = headers.transferEncoding
        if (encode != null && len != null) {
            throw IOException("Invalid Client Headers. Conflict Headers: \"${Headers.CONTENT_LENGTH}: $len\" and \"${Headers.TRANSFER_ENCODING}: $encode\"")
        }
        if (len != null) {
            val input = AsyncContentLengthInput(
                stream = channel.reader,
                contentLength = len,
                closeStream = false
            )
            readInput = input
            return input
        }
        if (encode != null) {
            if (encode.equals(Headers.CHUNKED, ignoreCase = true)) {
                val input = AsyncChunkedInput(
                    stream = channel.reader,
                    closeStream = false
                )
                readInput = input
                return input
            }
            throw IOException("Invalid ${Headers.TRANSFER_ENCODING}")
        }
        throw IOException("Invalid Client Headers")
    }

    override fun readText(): AsyncReader {
        val charset = headers.charset ?: throw IOException("Invalid Client Header: Missing Charset")
        return readBinary().bufferedReader(charset = Charsets.get(charset))
    }

    private suspend fun checkWebSocket() {
        if (!headers[Headers.CONNECTION]?.singleOrNull().equals(Headers.UPGRADE, true)) {
            rejectWebsocket()
            throw IllegalStateException("Invalid Client Headers: Invalid Header \"${Headers.CONNECTION}\"")
        }
        if (!headers[Headers.UPGRADE]?.singleOrNull().equals(Headers.WEBSOCKET, true)) {
            rejectWebsocket()
            throw IllegalStateException("Invalid Client Headers: Invalid Header \"${Headers.UPGRADE}\"")
        }
    }

    override suspend fun acceptWebsocket(): WebSocketConnection {
        checkClosed()
        checkWebSocket()
        val key = headers.getSingle(Headers.SEC_WEBSOCKET_KEY)
        if (key == null) {
            rejectWebsocket()
            throw IllegalStateException("Invalid Client Headers: Missing Header \"${Headers.SEC_WEBSOCKET_KEY}\"")
        }
        val sha1 = Sha1MessageDigest()
        val resp = response() as HttpResponse2Impl
        resp.headers[Headers.CONNECTION] = Headers.UPGRADE
        resp.headers[Headers.UPGRADE] = Headers.WEBSOCKET
        resp.headers[Headers.SEC_WEBSOCKET_ACCEPT] = HandshakeSecret.generateResponse(sha1, key)
        resp.sendHeadersAndFree()

        return ServerWebSocketConnection(
            input = channel.reader,
            output = channel.writer,
            rawConnection = channel.channel
        )
    }

    override suspend fun rejectWebsocket() {
        checkClosed()
        checkWebSocket()
        response().use {
            it.headers.keepAlive = false
            it.status = 403
        }
    }

    override suspend fun response(): HttpResponse {
        if (startedResponse != null) {
            throw IllegalStateException("Response already got")
        }
        ByteBuffer.alloc(DEFAULT_BUFFER_SIZE).use { buf ->
            if (readInput == null) {
                readBinary().use {
                    it.skipAll(buf)
                }
            }
        }
        var r = HttpResponse2Impl(this)
        startedResponse = r
        return r
    }

    private var startedResponse: HttpResponse2Impl? = null
    override val response: HttpResponse?
        get() = startedResponse

    override suspend fun asyncClose() {
        checkClosed()
        response().use {
            it.status = 404
        }
    }
}

internal class HttpResponse2Impl(val req: HttpRequest2Impl) : HttpResponse {
    override var status = 404
    override val headers = HashHeaders()

    init {
        headers.contentEncoding = when {
            req.server.zlibBufferSize > 0 && req.headers.acceptEncoding?.any {
                it.equals(
                    "gzip",
                    ignoreCase = true
                )
            } == true -> "gzip"
            req.server.zlibBufferSize > 0 && req.headers.acceptEncoding?.any {
                it.equals(
                    "deflate",
                    ignoreCase = true
                )
            } == true -> "deflate"
            else -> "identity"
        }
        headers.keepAlive = req.headers.keepAlive
    }

    private var closed = false
    private var responseStarted = false

    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("Already closed")
        }
    }

    private suspend fun sendRequest() {
        if (req.server.zlibBufferSize <= 0 && (
                    headers.contentEncoding.equals("gzip", ignoreCase = true)
                            || headers.contentEncoding.equals("deflate", ignoreCase = true)
                    )
        ) {
            throw IllegalStateException("Response doesn't support compress. Make sure you set HttpServer::zlibBufferSize more than 0")
        }
        req.channel.writer.append("HTTP/1.1 ")
            .append(status.toString()).append(" ")
            .append(statusToText(status))
            .append("\r\n")
        headers.forEachHeader { key, value ->
            req.channel.writer.append(key).append(": ").append(value).append("\r\n")
        }
        req.channel.writer.append("\r\n")
    }

    internal suspend fun sendHeadersAndFree() {
        checkClosed()
        sendRequest()
        closed = true
    }

    override suspend fun writeBinary(): AsyncOutput {
        checkClosed()
        responseStarted = true
        if (req.headers.keepAlive != headers.keepAlive) {
            throw IllegalStateException("Client not support Keep-Alive mode")
        }
        val len = headers.contentLength
        var encode = headers.transferEncoding

        if (len != null && encode != null) {
            throw IllegalStateException("Conflict Response Headers: \"${Headers.CONTENT_LENGTH}: $len\" and \"${Headers.CONTENT_ENCODING}: $encode\"")
        }
        fun selectEncode(): AsyncOutput {
            if (len != null) {
                closed = true
                return AsyncContentLengthOutput2(
                    req = req,
                    contentLength = len,
                    keepAlive = headers.keepAlive
                )
            }
            if (encode == null) {
                headers.transferEncoding = Headers.CHUNKED
                encode = Headers.CHUNKED
            }
            if (encode.equals(Headers.CHUNKED, ignoreCase = true)) {
                closed = true
                return AsyncChunkedOutput2(
                    req = req,
                    keepAlive = headers.keepAlive
                )
            }
            throw IllegalStateException("Unknown \"${Headers.TRANSFER_ENCODING}: $encode\"")
        }

        val stream = selectEncode()

        val enc = headers.contentEncoding
        val str = when {
            enc.equals("gzip", ignoreCase = true) -> AsyncGZIPOutput(
                stream = stream,
                level = 6,
                closeStream = true,
                bufferSize = req.server.zlibBufferSize
            )
            enc.equals("deflate", ignoreCase = true) -> AsyncDeflaterOutput(
                stream = stream,
                level = 6,
                closeStream = true,
                bufferSize = req.server.zlibBufferSize
            )
            else -> stream
        }
        sendRequest()
        closed = true
        return str
    }

    override suspend fun writeText(): AsyncWriter {
        val charset = Charsets.get(headers.charset ?: "utf-8")
        return writeBinary().bufferedWriter(charset = charset)
    }

    override suspend fun asyncClose() {
        if (closed && responseStarted) {
            return
        }
        checkClosed()
        if (headers.bodyExist) {
            throw IllegalStateException("Require Http Response Body")
        }
        headers.contentLength = 0uL
        sendRequest()
        req.channel.writer.flush()
        if (req.headers.keepAlive && headers.keepAlive) {
            req.server.clientProcessing(req.channel)
        } else {
            runCatching { req.channel.asyncClose() }
        }
    }

}

private class AsyncContentLengthOutput2(
    val req: HttpRequest2Impl,
    contentLength: ULong,
    val keepAlive: Boolean,
) : AsyncContentLengthOutput(
    closeStream = false,
    stream = req.channel.writer,
    contentLength = contentLength
) {
    override suspend fun asyncClose() {
        if (!isFull) {
            throw IllegalStateException("Not all content wrote")
        }
        super.asyncClose()
        if (keepAlive) {
            req.server.clientProcessing(req.channel)
        } else {
            req.channel.asyncClose()
        }
    }
}

private class AsyncChunkedOutput2(
    val req: HttpRequest2Impl,
    autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE,
    val keepAlive: Boolean,
) : AsyncChunkedOutput(
    stream = req.channel.writer,
    closeStream = false,
    autoFlushBuffer = autoFlushBuffer,
) {
    override suspend fun asyncClose() {
        super.asyncClose()
        if (keepAlive) {
            req.server.clientProcessing(req.channel)
        } else {
            req.channel.asyncClose()
        }
    }
}