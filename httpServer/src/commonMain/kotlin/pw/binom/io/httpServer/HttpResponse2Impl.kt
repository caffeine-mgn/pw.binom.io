package pw.binom.io.httpServer

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.charset.Charsets
import pw.binom.compression.zlib.AsyncDeflaterOutput
import pw.binom.compression.zlib.AsyncGZIPOutput
import pw.binom.io.AsyncWriter
import pw.binom.io.IOException
import pw.binom.io.http.AsyncContentLengthOutput
import pw.binom.io.http.Encoding
import pw.binom.io.http.HashHeaders
import pw.binom.io.http.forEachHeader
import pw.binom.pool.borrow

internal class HttpResponse2Impl(
    val keepAliveEnabled: Boolean,
    val channel: ServerAsyncAsciiChannel,
    val acceptEncoding: List<String>,
    var server: HttpServer
) : HttpResponse {
    override var status = 404
    override val headers = HashHeaders()

    init {
        headers.keepAlive = keepAliveEnabled
    }

    private var closed = false
    private var responseStarted = false

    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("Already closed")
        }
    }

    private suspend fun sendRequest() {
        if (server.zlibBufferSize <= 0 && (
            headers.contentEncoding.equals("gzip", ignoreCase = true) ||
                headers.contentEncoding.equals("deflate", ignoreCase = true)
            )
        ) {
            throw IllegalStateException("Response doesn't support compress. Make sure you set HttpServer::zlibBufferSize more than 0")
        }
        channel.writer.append("HTTP/1.1 ")
            .append(status.toString()).append(" ")
            .append(statusToText(status))
            .append("\r\n")
        headers.forEachHeader { key, value ->
            channel.writer.append(key).append(": ").append(value).append("\r\n")
        }
        channel.writer.append("\r\n")
    }

    internal suspend fun sendHeadersAndFree() {
        checkClosed()
        sendRequest()
        channel.writer.flush()
        closed = true
    }

    override suspend fun startWriteBinary(): AsyncOutput {
        checkClosed()
        responseStarted = true
        if (!keepAliveEnabled && headers.keepAlive) {
            throw IllegalStateException("Client not support Keep-Alive mode")
        }
        if (headers.contentEncoding == null && headers.getTransferEncodingList()
            .isEmpty() && headers.contentLength == null
        ) {
            val en = acceptEncoding
            headers.contentEncoding = when {
                "gzip" in en -> "gzip"
                "deflate" in en -> "deflate"
                else -> null
            }
            headers.transferEncoding = "chunked"
        }
        val transferEncoding = headers.getTransferEncodingList()
        val contentEncoding = headers.getContentEncodingList()

        val baseResponse = HttpResponseOutput(
            keepAlive = headers.keepAlive,
            server = server,
            channel = channel,
        )

        transferEncoding.forEach {
            if (it != "chunked" && it != "deflate" && it != "gzip" && it != "identity") {
                throw IOException("Not supported Transfer Encoding \"$it\"")
            }
        }

        fun wrap(name: String, stream: AsyncOutput) = when (name) {
            Encoding.IDENTITY -> stream
            Encoding.CHUNKED -> server.reusableAsyncChunkedOutputPool.new(
                stream = stream,
                closeStream = stream !== channel.writer,
            )
            Encoding.GZIP -> AsyncGZIPOutput(
                stream = stream,
                level = 6,
                closeStream = true,
                bufferSize = server.zlibBufferSize,
            )
            Encoding.DEFLATE -> AsyncDeflaterOutput(
                stream = stream,
                level = 6,
                closeStream = true,
                bufferSize = server.zlibBufferSize,
            )
            else -> null
        }

        var resultOutput: AsyncOutput = baseResponse
        val contentLength = headers.contentLength
        if (contentLength != null) {
            resultOutput = AsyncContentLengthOutput(
                stream = resultOutput,
                contentLength = contentLength,
                closeStream = true
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
        val charset = Charsets.get(headers.charset ?: "utf-8")
        val output = startWriteBinary()
        try {
            return server.bufferWriterPool.borrow {
                it.reset(
                    output = output,
                    charset = charset
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
//        if (headers.bodyExist && req.method.lowercase() != "head") {
//            throw IllegalStateException("Require Http Response Body")
//        }
        headers.contentLength = 0uL
        sendRequest()
        channel.writer.flush()
        if (keepAliveEnabled && headers.keepAlive) {
            server.clientReProcessing(channel)
        } else {
            runCatching { channel.asyncClose() }
        }
    }
}

private class HttpResponseOutput(
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

    override suspend fun writeFully(data: ByteBuffer) {
        channel.writer.writeFully(data)
    }

    override suspend fun flush() {
        channel.writer.flush()
    }
}
