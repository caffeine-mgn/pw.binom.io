package pw.binom.io.httpServer

import pw.binom.charset.Charsets
import pw.binom.compression.zlib.AsyncGZIPInput
import pw.binom.compression.zlib.AsyncInflateInput
import pw.binom.crypto.Sha1MessageDigest
import pw.binom.io.*
import pw.binom.io.http.*
import pw.binom.io.http.websocket.HandshakeSecret
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.net.Path
import pw.binom.net.Query
import pw.binom.net.toPath
import pw.binom.net.toQuery
import pw.binom.network.SocketClosedException
import pw.binom.pool.PoolObjectFactory
import pw.binom.pool.borrow
import pw.binom.skipAll

internal class HttpRequest2Impl(/*val onClose: (HttpRequest2Impl) -> Unit*/) : HttpRequest {
    object Manager : PoolObjectFactory<HttpRequest2Impl> {
        override fun free(value: HttpRequest2Impl) {
        }

        override fun new(): HttpRequest2Impl = HttpRequest2Impl()
    }

    companion object {
        suspend fun read(
            channel: ServerAsyncAsciiChannel,
            server: HttpServer,
            isNewConnect: Boolean
        ): HttpRequest2Impl {
            if (!isNewConnect) {
                server.browConnection(channel)
            }
            val request = channel.reader.readln() ?: throw SocketClosedException()
            val items = request.split(' ', limit = 3)
            val requestObject = server.httpRequest2Impl.borrow()
            val headers = requestObject.internalHeaders
            headers.clear()
            while (true) {
                val s = channel.reader.readln() ?: break
                if (s.isEmpty()) {
                    break
                }
                val p = s.indexOf(':')
                if (p < 0) {
                    runCatching { channel.asyncClose() }
                    throw IOException("Invalid HTTP Header: \"$s\"")
                }
                val headerKey = s.substring(0, p)
                val headerValue = s.substring(p + 2)
                headers.add(headerKey, headerValue)
            }
            requestObject.reset(
                request = (items.getOrNull(1) ?: ""),
                method = items[0],
                channel = channel,
                server = server,
            )
            return requestObject
        }
    }

    var channel: ServerAsyncAsciiChannel? = null
    var server: HttpServer? = null

    private var internalHeaders = HashHeaders()
    override var method: String = ""

    override var request: String = ""
        private set

    private var closed = false
    private var readInput: AsyncInput? = null

    override val headers: Headers
        get() = internalHeaders
    override val path: Path
        get() {
            val p = request.indexOf('?')
            return if (p >= 0) {
                request.substring(0, p).toPath
            } else {
                request.toPath
            }
        }

    override val query: Query?
        get() {
            val p = request.indexOf('?')
            return if (p < 0) {
                null
            } else {
                request.substring(p + 1).toQuery
            }
        }
    private var startedResponse: HttpResponse2Impl? = null
    override suspend fun <T> response(func: suspend (HttpResponse) -> T): T {
        return super.response(func)
    }

    override val response: HttpResponse?
        get() = startedResponse
    override var isReadyForResponse: Boolean = true
        private set

    fun reset(
        request: String,
        method: String,
        channel: ServerAsyncAsciiChannel,
        server: HttpServer,
    ) {
        this.request = request
        this.method = method
        this.channel = channel
        this.server = server
        closed = false
        isReadyForResponse = true
        internalHeaders.clear()
    }

    fun free() {
        startedResponse?.free()
        startedResponse = null
        channel = null
        server = null
        readInput = null
//        onClose(this)
    }

    private fun checkClosed() {
        if (closed) {
            throw ClosedException()
        }
    }

    override fun readBinary(): AsyncInput {
        checkClosed()
        if (readInput != null) {
            throw IllegalStateException("Already reading")
        }
        if (!headers.bodyExist) {
            readInput = AsyncEmptyHttpInput
            return AsyncEmptyHttpInput
        }
        val contentLength = headers.contentLength
        val transferEncoding = headers.getTransferEncodingList()
        var stream: AsyncInput = channel!!.reader
        if (contentLength != null) {
            stream = AsyncContentLengthInput(
                stream = stream,
                contentLength = contentLength,
                closeStream = false
            )
        }

        fun wrap(name: String, stream: AsyncInput) = when (name) {
            Encoding.IDENTITY -> stream
            Encoding.CHUNKED -> AsyncChunkedInput(
                stream = stream,
                closeStream = false,
            )
            Encoding.GZIP -> AsyncGZIPInput(
                stream = stream,
                closeStream = false,
            )
            Encoding.DEFLATE -> AsyncInflateInput(
                stream = stream,
                wrap = true,
                closeStream = false
            )
            else -> null
        }

        for (i in transferEncoding.lastIndex downTo 0) {
            stream = wrap(name = transferEncoding[i], stream = stream)
                ?: throw IOException("Not supported encoding \"${transferEncoding[i]}\"")
        }
        readInput = stream
        return stream
    }

    override fun readText(): AsyncReader {
        val charset = headers.charset ?: "utf-8"
        return readBinary().bufferedReader(charset = Charsets.get(charset))
    }

    private suspend fun checkTcp() {
        if (!headers[Headers.CONNECTION]?.singleOrNull().equals(Headers.UPGRADE, true)) {
            rejectWebsocket()
            throw IllegalStateException("Invalid Client Headers: Invalid Header \"${Headers.CONNECTION}\"")
        }
        if (!headers[Headers.UPGRADE]?.singleOrNull().equals(Headers.TCP, true)) {
            rejectWebsocket()
            throw IllegalStateException("Invalid Client Headers: Invalid Header \"${Headers.UPGRADE}\"")
        }
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

    override suspend fun acceptWebsocket(masking: Boolean): WebSocketConnection {
        val server = server!!
        val channel = channel!!
        checkClosed()
        checkWebSocket()
        val key = headers.getSingleOrNull(Headers.SEC_WEBSOCKET_KEY)
        if (key == null) {
            rejectWebsocket()
            throw IllegalStateException("Invalid Client Headers: Missing Header \"${Headers.SEC_WEBSOCKET_KEY}\"")
        }
        val sha1 = Sha1MessageDigest()
        val resp = response() as HttpResponse2Impl
        resp.status = 101
        resp.headers[Headers.CONNECTION] = Headers.UPGRADE
        resp.headers[Headers.UPGRADE] = Headers.WEBSOCKET
        resp.headers[Headers.SEC_WEBSOCKET_ACCEPT] = HandshakeSecret.generateResponse(sha1, key)
        resp.sendHeaders()
        isReadyForResponse = false
        return server.webSocketConnectionPool.new(
            input = channel.reader,
            output = channel.writer,
            masking = masking,
        )
    }

    override suspend fun rejectWebsocket() {
        checkClosed()
        response().use {
            it.headers.keepAlive = false
            it.status = 403
        }
    }

    override suspend fun acceptTcp(): AsyncChannel {
        checkClosed()
        checkTcp()
        val channel = channel!!
        val resp = response() as HttpResponse2Impl
        resp.status = 101
        resp.headers[Headers.CONNECTION] = Headers.UPGRADE
        resp.headers[Headers.UPGRADE] = Headers.WEBSOCKET
        resp.sendHeadersAndFree()
        isReadyForResponse = false
        return channel.channel
    }

    override suspend fun rejectTcp() {
        checkClosed()
        response().use {
            it.headers.keepAlive = false
            it.status = 403
        }
    }

    override suspend fun response(): HttpResponse {
        checkClosed()
        if (startedResponse != null) {
            throw IllegalStateException("Response already got")
        }
        val server = server ?: throw SocketClosedException()
        if (readInput == null) {
            server.textBufferPool.using { buf ->
                readBinary().use {
                    it.skipAll(buf)
                }
            }
        }
        val r = server.httpResponse2Impl.borrow {
            it.reset(
                keepAliveEnabled = server.maxIdleTime.isPositive() && headers.keepAlive,
                channel = channel!!,
                acceptEncoding = headers.acceptEncoding,
                server = server,
                onclosed = { }
            )
        }
        startedResponse = r
        closed = true
        isReadyForResponse = false
        return r
    }

    override suspend fun asyncClose() {
        checkClosed()
        response {
            it.status = 404
        }
    }
}
