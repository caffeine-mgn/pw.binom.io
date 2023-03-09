package pw.binom.io.httpServer

import pw.binom.ByteBufferPool
import pw.binom.compression.zlib.AsyncDeflaterOutput
import pw.binom.compression.zlib.AsyncGZIPInput
import pw.binom.compression.zlib.AsyncGZIPOutput
import pw.binom.compression.zlib.AsyncInflateInput
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.AsyncReader
import pw.binom.io.IOException
import pw.binom.io.http.*
import pw.binom.url.Path
import pw.binom.url.Query
import pw.binom.url.toPath
import pw.binom.url.toQuery
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal fun interface IdlePool {
    @Suppress("FUN_INTERFACE_WITH_SUSPEND_FUNCTION")
    suspend fun returnToPool(channel: ServerAsyncAsciiChannel)
}

object HttpServerUtils {
    @Suppress("OPT_IN_IS_NOT_ENABLED")
    @OptIn(ExperimentalContracts::class)
    inline fun <T> parseHttpRequest(request: String, action: (method: String, path: String) -> T): T {
        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }
        val items = request.split(' ', limit = 3)
        return action(items[0], items.getOrNull(1) ?: "")
    }

    inline fun extractPathFromRequest(request: String): Path {
        val p = request.indexOf('?')
        return if (p >= 0) {
            request.substring(0, p).toPath
        } else {
            request.toPath
        }
    }

    inline fun extractQueryFromRequest(request: String): Query? {
        val p = request.indexOf('?')
        return if (p < 0) {
            null
        } else {
            request.substring(p + 1).toQuery
        }
    }

    fun statusCodeToDescription(code: Int) = statusToText(code)

    fun wrapStream(
        encoding: String,
        stream: AsyncOutput,
        compressBufferPool: ByteBufferPool?,
        closeStream: Boolean,
        compressLevel: Int,
    ) =
        when {
            encoding == Encoding.IDENTITY -> stream
            encoding == Encoding.CHUNKED -> AsyncChunkedOutput(
                stream = stream,
                closeStream = closeStream,
            )

            compressBufferPool != null && encoding == Encoding.GZIP -> AsyncGZIPOutput(
                stream = stream,
                level = compressLevel,
                closeStream = closeStream,
                bufferPool = compressBufferPool,
            )

            compressBufferPool != null && encoding == Encoding.DEFLATE -> AsyncDeflaterOutput(
                stream = stream,
                level = compressLevel,
                closeStream = closeStream,
                bufferPool = compressBufferPool,
            )

            else -> null
        }

    fun wrapStream(encoding: String, stream: AsyncInput, closeStream: Boolean) = when (encoding) {
        Encoding.IDENTITY -> stream
        Encoding.CHUNKED -> AsyncChunkedInput(
            stream = stream,
            closeStream = closeStream,
        )

        Encoding.GZIP -> AsyncGZIPInput(
            stream = stream,
            closeStream = closeStream,
        )

        Encoding.DEFLATE -> AsyncInflateInput(
            stream = stream,
            wrap = true,
            closeStream = closeStream,
        )

        else -> null
    }

    suspend fun readHeaders(channel: AsyncAsciiChannel): HashHeaders {
        val headers = HashHeaders()
        try {
            readHeaders(dest = headers, reader = channel.reader)
        } catch (e: Throwable) {
            channel.asyncCloseAnyway()
            throw e
        }
        return headers
    }

    suspend fun readHeaders(dest: MutableHeaders, reader: AsyncReader) {
        while (true) {
            val s = reader.readln() ?: break
            if (s.isEmpty()) {
                break
            }
            val p = s.indexOf(':')
            if (p < 0) {
                throw IOException("Invalid HTTP Header: \"$s\"")
            }
            val headerKey = s.substring(0, p)
            val headerValue = s.substring(p + 2)
            dest.add(headerKey, headerValue)
        }
    }
}
