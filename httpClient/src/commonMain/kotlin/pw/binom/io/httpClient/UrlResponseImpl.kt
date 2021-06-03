package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.net.URI
import pw.binom.compression.zlib.AsyncGZIPInput
import pw.binom.compression.zlib.AsyncInflateInput
import pw.binom.io.IOException
import pw.binom.io.http.AsyncChunkedInput
import pw.binom.io.http.AsyncContentLengthInput
import pw.binom.io.http.AsyncHttpInput
import pw.binom.io.http.Headers

@Deprecated(message = "Use HttpClient", level = DeprecationLevel.WARNING)
internal class UrlResponseImpl(
    override val responseCode: Int,
    override val headers: Map<String, List<String>>,
    val URI: URI,
    val channel: AsyncHttpClient.Connection,
    val input: AsyncInput,
    val client: AsyncHttpClient
) : AsyncHttpClient.UrlResponse {

    private var keepAlive = headers[Headers.CONNECTION]?.singleOrNull()?.lowercase() == Headers.KEEP_ALIVE.lowercase()

    private val httpStream: AsyncHttpInput = run {
        if (headers[Headers.TRANSFER_ENCODING]?.singleOrNull() == Headers.CHUNKED) {
            return@run AsyncChunkedInput(
                    input,
                    closeStream = false
            )
        }

        val contentLength = headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull()
        if (contentLength != null)
            return@run AsyncContentLengthInput(
                    stream = input,
                    contentLength = contentLength
            )
        keepAlive = false
        return@run ClosableAsyncInput(input)
    }
    private val stream = run {
        val contentEncode = headers[Headers.CONTENT_ENCODING]?.lastOrNull()?.lowercase()

        when (contentEncode) {
            "gzip" -> AsyncGZIPInput(httpStream, closeStream = true)
            "deflate" -> AsyncInflateInput(httpStream, wrap = true, closeStream = true)
            "identity", null -> httpStream
            else -> throw IOException("Unknown Encoding: \"$contentEncode\"")
        }
    }

    override val available: Int
        get() = stream.available

    override suspend fun read(dest: ByteBuffer): Int =
            stream.read(dest)

    override suspend fun asyncClose() {
        input.asyncClose()
        if (!httpStream.isEof) {
            stream.asyncClose()
            channel.asyncClose()
        } else {
            if (keepAlive) {
                client.recycleConnection(URI, channel)
            } else {
                stream.asyncClose()
                channel.asyncClose()
            }
        }
    }

}