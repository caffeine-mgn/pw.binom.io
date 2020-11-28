package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.URL
import pw.binom.compression.zlib.AsyncGZIPInput
import pw.binom.compression.zlib.AsyncInflateInput
import pw.binom.io.IOException
import pw.binom.io.http.AsyncChunkedInput
import pw.binom.io.http.AsyncContentLengthInput
import pw.binom.io.http.AsyncHttpInput
import pw.binom.io.http.Headers

internal class UrlResponseImpl(
        override val responseCode: Int,
        override val headers: Map<String, List<String>>,
        val url: URL,
        val channel: AsyncHttpClient.Connection,
        val input: AsyncInput,
        val client: AsyncHttpClient
) : AsyncHttpClient.UrlResponse {

    private var keepAlive = headers[Headers.CONNECTION]?.singleOrNull()?.toLowerCase() == Headers.KEEP_ALIVE.toLowerCase()

    private val httpStream: AsyncHttpInput = run {
        if (headers[Headers.TRANSFER_ENCODING]?.singleOrNull() == Headers.CHUNKED) {
            return@run AsyncChunkedInput(
                    input,
                    autoCloseStream = false
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
        val contentEncode = headers[Headers.CONTENT_ENCODING]?.lastOrNull()?.toLowerCase()

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

    override suspend fun close() {
        input.close()
        if (!httpStream.isEof) {
            stream.close()
            channel.close()
        } else {
            if (keepAlive) {
                client.recycleConnection(url, channel)
            } else {
                stream.close()
                channel.close()
            }
        }
    }

}