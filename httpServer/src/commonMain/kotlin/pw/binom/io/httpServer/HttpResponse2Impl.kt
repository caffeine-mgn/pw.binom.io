package pw.binom.io.httpServer

import pw.binom.charset.Charsets
import pw.binom.compression.zlib.AsyncDeflaterOutput
import pw.binom.compression.zlib.AsyncGZIPOutput
import pw.binom.io.AsyncOutput
import pw.binom.io.AsyncWriter
import pw.binom.io.ByteBuffer
import pw.binom.io.IOException
import pw.binom.io.http.*
import pw.binom.pool.ObjectFactory
import pw.binom.pool.ObjectPool

internal class HttpResponse2Impl(
    val onClose: (HttpResponse2Impl) -> Unit,
) : HttpResponse {

    object Manager : ObjectFactory<HttpResponse2Impl> {
        override fun deallocate(value: HttpResponse2Impl, pool: ObjectPool<HttpResponse2Impl>) {
        }

        override fun allocate(pool: ObjectPool<HttpResponse2Impl>): HttpResponse2Impl =
            HttpResponse2Impl { pool.recycle(it) }
    }

    override var status = 404
    override val headers = HashHeaders()

    var keepAliveEnabled: Boolean = false
    var channel: ServerAsyncAsciiChannel? = null
    var acceptEncoding: List<String> = emptyList()
    var server: HttpServer? = null
    private var closed = false
    private var responseStarted = false
    private var onclosed2: (() -> Unit)? = null

    fun reset(
        keepAliveEnabled: Boolean,
        channel: ServerAsyncAsciiChannel,
        acceptEncoding: List<String>,
        server: HttpServer,
        onclosed: (() -> Unit),
    ) {
        headers.clear()
        headers.keepAlive = keepAliveEnabled
        this.keepAliveEnabled = keepAliveEnabled
        this.channel = channel
        this.acceptEncoding = acceptEncoding
        this.server = server
        closed = false
        responseStarted = false
        status = 404
        this.onclosed2 = onclosed
    }

    internal fun free() {
        onclosed2?.invoke()
        onClose(this)
        channel = null
        server = null
        onclosed2 = null
    }

    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("Already closed")
        }
    }

    private suspend fun sendRequest() {
        val contentEncoding = headers.contentEncoding
        val isCompressed = contentEncoding.equals(other = "gzip", ignoreCase = true) || contentEncoding.equals(
            other = "deflate",
            ignoreCase = true,
        )
        if (server!!.zlibBufferSize <= 0 && isCompressed) {
            throw IllegalStateException("Response doesn't support compress. Make sure you set HttpServer::zlibBufferSize more than 0")
        }
        channel!!.writer.append("HTTP/1.1 ").append(statusInt(status)).append(" ")
            .append(HttpServerUtils.statusCodeToDescription(status))
            .append(Utils.CRLF)
        headers.forEachHeader { key, value ->
            channel!!.writer.append(key).append(": ").append(value).append(Utils.CRLF)
        }
        channel!!.writer.append(Utils.CRLF)
    }

    internal suspend fun sendHeaders() {
        checkClosed()
        sendRequest()
        channel!!.writer.flush()
        closed = true
    }

    internal suspend fun sendHeadersAndFree() {
        checkClosed()
        sendRequest()
        channel!!.writer.flush()
        closed = true
        free()
    }

    override suspend fun startWriteBinary(): AsyncOutput {
        checkClosed()
        responseStarted = true
        if (!keepAliveEnabled && headers.keepAlive) {
            throw IllegalStateException("Client not support Keep-Alive mode")
        }
        if (headers.contentEncoding == null &&
            headers.getTransferEncodingList().isEmpty() &&
            headers.contentLength == null
        ) {
            val en = acceptEncoding
            if (server!!.zlibBufferSize > 0) {
                headers.contentEncoding = when {
                    "gzip" in en -> "gzip"
                    "deflate" in en -> "deflate"
                    else -> null
                }
            }
            headers.transferEncoding = "chunked"
        }
        val transferEncoding = headers.getTransferEncodingList()
        val contentEncoding = headers.getContentEncodingList()

        val baseResponse = HttpResponseOutput(
            keepAlive = headers.keepAlive,
            server = server!!,
            channel = channel!!,
        )

        transferEncoding.forEach {
            if (it != "chunked" && it != "deflate" && it != "gzip" && it != "identity") {
                throw IOException("Not supported Transfer Encoding \"$it\"")
            }
        }

        fun wrap(name: String, stream: AsyncOutput) = when (name) {
            Encoding.IDENTITY -> stream
            Encoding.CHUNKED -> server!!.reusableAsyncChunkedOutputPool.borrow().also {
                it.reset(
                    stream = stream,
                    closeStream = stream !== channel!!.writer,
                )
            }

            Encoding.GZIP -> AsyncGZIPOutput(
                stream = stream,
                level = 6,
                closeStream = true,
                bufferPool = server!!.compressBufferPool,
            )

            Encoding.DEFLATE -> AsyncDeflaterOutput(
                stream = stream,
                level = 6,
                closeStream = true,
                bufferPool = server!!.compressBufferPool,
            )

            else -> null
        }

        var resultOutput: AsyncOutput = baseResponse
        val contentLength = headers.contentLength
        if (contentLength != null) {
            resultOutput = AsyncContentLengthOutput(
                stream = resultOutput,
                contentLength = contentLength,
                closeStream = true,
            )
        }
        for (i in transferEncoding.lastIndex downTo 0) {
            val it = transferEncoding[i]
            resultOutput = wrap(it, resultOutput) ?: throw IOException("Not supported encoding \"$it\"")
        }

        for (i in contentEncoding.lastIndex downTo 0) {
            val it = contentEncoding[i]
            resultOutput = wrap(it, resultOutput) ?: throw IOException("Not supported encoding \"$it\"")
        }

        sendRequest()
        closed = true
        return resultOutput
    }

    override suspend fun startWriteText(): AsyncWriter {
        val server = server ?: throw IllegalStateException("Server not set")
        val charset = Charsets.get(headers.charset ?: "utf-8")
        val output = startWriteBinary()
        try {
            return server.bufferWriterPool.borrow().also {
                it.reset(
                    output = output,
                    charset = charset,
                )
            }
        } catch (e: Throwable) {
            try {
                output.asyncClose()
            } catch (ex: Throwable) {
                ex.addSuppressed(e)
                throw ex
            }
            throw e
        }
    }

    override suspend fun asyncClose() {
        if (closed && responseStarted) {
            return
        }
        checkClosed()
        try {
//        if (headers.bodyExist && req.method.lowercase() != "head") {
//            throw IllegalStateException("Require Http Response Body")
//        }
            headers.contentLength = 0uL
            sendRequest()
            channel!!.writer.flush()
            if (keepAliveEnabled && headers.keepAlive) {
                server!!.clientReProcessing(channel!!)
            } else {
                channel!!.asyncCloseAnyway()
            }
        } finally {
            free()
        }
    }
}

internal class HttpResponseOutput(
    val server: HttpServer,
    val channel: ServerAsyncAsciiChannel,
    val keepAlive: Boolean,
) : AsyncOutput {
    override suspend fun write(data: ByteBuffer): Int =
        channel.writer.write(data)

    override suspend fun asyncClose() {
        flush()
        if (keepAlive) {
            server.clientReProcessing(channel)
        } else {
            channel.asyncClose()
        }
    }

    override suspend fun writeFully(data: ByteBuffer) = channel.writer.writeFully(data)

    override suspend fun flush() {
        channel.writer.flush()
    }
}

internal class HttpResponseOutput2(
    val channel: ServerAsyncAsciiChannel,
    val keepAlive: Boolean,
    val returnToIdle: IdlePool?,
) : AsyncOutput {
    override suspend fun write(data: ByteBuffer): Int =
        channel.writer.write(data)

    override suspend fun asyncClose() {
        println("HttpResponseOutput2::Closed!")
        flush()
        if (keepAlive && returnToIdle != null) {
            returnToIdle.returnToPool(channel)
        } else {
            channel.asyncClose()
        }
    }

    override suspend fun writeFully(data: ByteBuffer) = channel.writer.writeFully(data)

    override suspend fun flush() {
        channel.writer.flush()
    }
}
