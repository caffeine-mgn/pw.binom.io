package pw.binom.io.httpServer

import pw.binom.ByteBufferPool
import pw.binom.atomic.AtomicBoolean
import pw.binom.charset.Charsets
import pw.binom.io.*
import pw.binom.io.http.*

@Deprecated(message = "Use HttpServer2")
internal class HttpResponse3Impl(
    val keepAliveEnabled: Boolean,
    val channel: ServerAsyncAsciiChannel,
    val acceptEncoding: List<String>,
    val returnToIdle: IdlePool?,
    val compressBufferPool: ByteBufferPool,
    val textBufferPool: ByteBufferPool,
    val charBufferSize: Int,
) : HttpResponse {
    override var status: Int = 404
    override val headers: MutableHeaders = HashHeaders()
    private var closed = AtomicBoolean(false)

    private fun checkClosed() {
        if (closed.getValue()) {
            throw ClosedException()
        }
    }

    private suspend fun sendRequest() {
//        val contentEncoding = headers.contentEncoding
//        val isCompressed = contentEncoding.equals(other = "gzip", ignoreCase = true) || contentEncoding.equals(
//            other = "deflate",
//            ignoreCase = true
//        )
//        if (server!!.zlibBufferSize <= 0 && isCompressed) {
//            throw IllegalStateException("Response doesn't support compress. Make sure you set HttpServer::zlibBufferSize more than 0")
//        }
        channel.writer.append("HTTP/1.1 ").append(statusInt(status)).append(" ")
            .append(HttpServerUtils.statusCodeToDescription(status))
            .append(Utils.CRLF)
        headers.forEachHeader { key, value ->
            channel.writer.append(key).append(": ").append(value).append(Utils.CRLF)
        }
        channel.writer.append(Utils.CRLF)
    }

    internal suspend fun sendHeaders() {
        sendRequest()
        channel.writer.flush()
        closed.setValue(true)
    }

    override suspend fun startWriteBinary(): AsyncOutput {
        checkClosed()
        if (!keepAliveEnabled && (headers.keepAlive ?: true)) {
            throw IllegalStateException("Client not support Keep-Alive mode")
        }
        if (headers.contentEncoding == null &&
            headers.getTransferEncodingList().isEmpty() &&
            headers.contentLength == null
        ) {
//            if (server!!.zlibBufferSize > 0) {
//                headers.contentEncoding = when {
//                    "gzip" in acceptEncoding -> "gzip"
//                    "deflate" in acceptEncoding -> "deflate"
//                    else -> null
//                }
//            }
            headers.transferEncoding = "chunked"
        }
        val transferEncoding = headers.getTransferEncodingList()
        val contentEncoding = headers.getContentEncodingList()

        val baseResponse = HttpResponseOutput2(
            keepAlive = headers.keepAlive ?: true,
            returnToIdle = returnToIdle,
            channel = channel,
        )

        transferEncoding.forEach {
            if (it != "chunked" && it != "deflate" && it != "gzip" && it != "identity") {
                throw IOException("Not supported Transfer Encoding \"$it\"")
            }
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
            resultOutput = HttpServerUtils.wrapStream(
                encoding = it,
                stream = resultOutput,
                closeStream = true,
                compressBufferPool = compressBufferPool,
                compressLevel = 6,
            )
                ?: throw IOException("Not supported encoding \"$it\"")
        }

        for (i in contentEncoding.lastIndex downTo 0) {
            val it = contentEncoding[i]
            resultOutput = HttpServerUtils.wrapStream(
                encoding = it,
                stream = resultOutput,
                closeStream = true,
                compressBufferPool = compressBufferPool,
                compressLevel = 6,
            )
                ?: throw IOException("Not supported encoding \"$it\"")
        }

        sendRequest()
        closed.setValue(true)
        return resultOutput
    }

    override suspend fun startWriteText(): AsyncWriter {
//        val server = server ?: throw IllegalStateException("Server not set")
        val charset = Charsets.get(headers.charset ?: "utf-8")
        val output = startWriteBinary()
        return output.bufferedWriter(
            pool = textBufferPool,
            charset = charset,
            charBufferSize = charBufferSize,
        )
//        try {
//            return server.bufferWriterPool.borrow().also {
//                it.reset(
//                    output = output,
//                    charset = charset
//                )
//            }
//        } catch (e: Throwable) {
//            try {
//                output.asyncClose()
//            } catch (ex: Throwable) {
//                ex.addSuppressed(e)
//                throw ex
//            }
//            throw e
//        }
    }

    override suspend fun asyncClose() {
//        if (closed.getValue() && responseStarted) {
//            return
//        }
//        checkClosed()
        if (!closed.compareAndSet(false, true)) {
            return
        }
//        if (headers.bodyExist && req.method.lowercase() != "head") {
//            throw IllegalStateException("Require Http Response Body")
//        }
        headers.contentLength = 0uL
        sendRequest()
        channel.writer.flush()
        if (keepAliveEnabled && (headers.keepAlive ?: true) && returnToIdle != null) {
            returnToIdle.returnToPool(channel)
        } else {
            channel.asyncCloseAnyway()
        }
    }
}
