package pw.binom.io.httpServer

import kotlinx.coroutines.Job
import kotlinx.coroutines.withTimeoutOrNull
import pw.binom.compression.zlib.AsyncGZIPInput
import pw.binom.compression.zlib.AsyncInflateInput
import pw.binom.concurrency.synchronize
import pw.binom.crypto.Sha1MessageDigest
import pw.binom.io.*
import pw.binom.io.http.*
import pw.binom.io.http.websocket.HandshakeSecret
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.network.SocketClosedException
import pw.binom.pool.ObjectFactory
import pw.binom.pool.ObjectPool
import pw.binom.pool.tryBorrow
import pw.binom.pool.using
import pw.binom.url.Path
import pw.binom.url.Query
import pw.binom.url.toPath
import pw.binom.url.toQuery
import kotlin.time.Duration

@Deprecated(message = "Use HttpServer2")
internal class HttpRequest2Impl(/*val onClose: (HttpRequest2Impl) -> Unit*/) : HttpRequest {
    object Manager : ObjectFactory<HttpRequest2Impl> {
        override fun deallocate(value: HttpRequest2Impl, pool: ObjectPool<HttpRequest2Impl>) {
        }

        override fun allocate(pool: ObjectPool<HttpRequest2Impl>): HttpRequest2Impl = HttpRequest2Impl()
    }

    companion object {
        suspend fun read(
            channel: ServerAsyncAsciiChannel,
            server: HttpServer,
            isNewConnect: Boolean,
            readStartTimeout: Duration,
            idleJob: Job?,
        ): Result<HttpRequest2Impl?> = runCatching {
            val request = if (readStartTimeout.isInfinite() || readStartTimeout == Duration.ZERO) {
                channel.reader.readln()
            } else {
                withTimeoutOrNull(readStartTimeout) { channel.reader.readln() }
            }
//            val request = channel.reader.readln()
            if (idleJob != null) {
                server.idleJobsLock.synchronize {
                    if (!server.idleJobs.remove(idleJob)) {
                        return@runCatching null
                    }
                }
            }
            if (!isNewConnect) {
                server.browConnection(channel)
            }
            if (request == null) {
                channel.asyncCloseAnyway()
                return@runCatching null
            }
            HttpServerMetrics.httpRequestCounter.inc()
            val items = request.split(' ', limit = 3)
            server.httpRequest2Impl.tryBorrow { requestObject ->
                val headers = requestObject.internalHeaders
                headers.clear()
                try {
                    HttpServerUtils.readHeaders(dest = headers, reader = channel.reader)
                } catch (e: Throwable) {
                    channel.asyncCloseAnyway()
                    throw e
                }

                requestObject.reset(
                    request = (items.getOrNull(1) ?: ""),
                    method = items[0],
                    channel = channel,
                    server = server,
                )
                return@runCatching requestObject
            }
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
    override val path: Path by lazy {
        val p = request.indexOf('?')
        if (p >= 0) {
            request.substring(0, p).toPath
        } else {
            request.toPath
        }
    }

    override val query: Query? by lazy {
        val p = request.indexOf('?')
        if (p < 0) {
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
                closeStream = false,
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
                closeStream = false,
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

    private suspend fun checkTcp() {
        if (!hasUpgrade()) {
            sendReject()
            throw IllegalStateException("Invalid Client Headers: Header \"${Headers.CONNECTION}: ${Headers.UPGRADE}\" not found")
        }

        if (!headers[Headers.UPGRADE]?.singleOrNull().equals(Headers.TCP, true)) {
            sendReject()
            throw IllegalStateException("Invalid Client Headers: Invalid Header \"${Headers.UPGRADE}\"")
        }
    }

    private fun hasUpgrade() = headers[Headers.CONNECTION]
        ?.asSequence()
        ?.flatMap { it.splitToSequence(',') }
        ?.map { it.trim() }
        ?.any { it.equals(Headers.UPGRADE, true) }
        ?: false

    private suspend fun checkWebSocket() {
        val connection = headers[Headers.CONNECTION]
        if (connection == null) {
            sendReject()
            throw IllegalStateException("Invalid Client Headers: Missing Header \"${Headers.CONNECTION}\"")
        }
        if (!hasUpgrade()) {
            sendReject()
            throw IllegalStateException("Invalid Client Headers: Header \"${Headers.CONNECTION}: ${Headers.UPGRADE}\" not found")
        }
//        if (connection.size > 1) {
//            rejectWebsocket()
//            throw IllegalStateException("Invalid Client Headers: Several headers \"${Headers.CONNECTION}\"")
//        }
//        val connectionValue = connection.single()
//        if (!connectionValue.equals(Headers.UPGRADE, true)) {
//            connectionValue.split(',').asSequence().map { it.trim() }.any { it.equals(Headers.UPGRADE, true) }
//            rejectWebsocket()
//            throw IllegalStateException("Invalid Client Headers: Header \"${Headers.CONNECTION}\" has value \"$connectionValue\". Excepted \"${Headers.UPGRADE}\"")
//        }
        if (!headers[Headers.UPGRADE]?.singleOrNull().equals(Headers.WEBSOCKET, true)) {
            sendReject()
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
            sendReject()
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

    override suspend fun acceptTcp(): AsyncChannel {
        checkClosed()
        checkTcp()
        val channel = channel!!
        val resp = response() as HttpResponse2Impl
        resp.status = 101
        resp.headers[Headers.CONNECTION] = Headers.UPGRADE
        resp.headers[Headers.UPGRADE] = Headers.TCP
        resp.sendHeadersAndFree()
        isReadyForResponse = false
        return channel.channel
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
        val r = server.httpResponse2Impl.borrow().also {
            it.reset(
                keepAliveEnabled = server.maxIdleTime.isPositive() && (headers.keepAlive ?: true),
                channel = channel!!,
                acceptEncoding = headers.acceptEncoding,
                server = server,
                onclosed = { },
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
