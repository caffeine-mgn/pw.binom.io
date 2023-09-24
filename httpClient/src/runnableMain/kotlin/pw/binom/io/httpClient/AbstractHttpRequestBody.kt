package pw.binom.io.httpClient

import pw.binom.io.AsyncOutput
import pw.binom.io.IOException
import pw.binom.io.http.AsyncChunkedOutput
import pw.binom.io.http.AsyncContentLengthOutput
import pw.binom.io.http.Encoding
import pw.binom.io.http.Headers

abstract class AbstractHttpRequestBody : HttpRequestBody {
    protected abstract val headers: Headers
    private var internalIsOutputStarted = false
    protected var internalIsFlushed = false
    override val isOutputStarted: Boolean
        get() = internalIsOutputStarted
    override val isFlushed: Boolean
        get() = internalIsFlushed
    protected abstract val autoFlushBuffer: Int

    override suspend fun startWriteBinary(): AsyncOutput {
        check(!internalIsOutputStarted) { "Output already started" }
        check(!internalIsFlushed) { "Request already flushed" }
        val encode = headers.transferEncoding
        if (encode != null) {
            when (encode.lowercase()) {
                Encoding.CHUNKED -> {
                    internalIsOutputStarted = true
                    HttpMetrics.defaultHttpRequestCountMetric.dec()
                    return AsyncChunkedOutput(
                        stream = output,
                        autoFlushBuffer = autoFlushBuffer,
                        closeStream = false,
                    )
                }

                else -> throw IOException("Unknown Transfer Encoding \"$encode\"")
            }
        }
        val len = headers.contentLength
        if (len != null) {
            internalIsOutputStarted = true
            HttpMetrics.defaultHttpRequestCountMetric.dec()
            return AsyncContentLengthOutput(
                stream = output,
                closeStream = false,
                contentLength = len,
            )
        }
        throw IllegalStateException("Unknown type of Transfer Encoding")
    }
}
