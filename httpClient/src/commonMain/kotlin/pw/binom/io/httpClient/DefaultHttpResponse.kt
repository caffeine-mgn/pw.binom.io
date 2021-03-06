package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.URL
import pw.binom.charset.Charsets
import pw.binom.compression.zlib.AsyncGZIPInput
import pw.binom.compression.zlib.AsyncInflateInput
import pw.binom.io.AsyncReader
import pw.binom.io.EOFException
import pw.binom.io.IOException
import pw.binom.io.bufferedReader
import pw.binom.io.http.HashHeaders
import pw.binom.io.http.Headers

class DefaultHttpResponse(
    val url: URL,
    val client: HttpClient,
    var keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    override val responseCode: Int,
    override val headers: Headers
) : HttpResponse {
    companion object {
        suspend fun read(
            url: URL,
            client: HttpClient,
            keepAlive: Boolean,
            channel: AsyncAsciiChannel,
        ): HttpResponse {
            val title = channel.reader.readln() ?: throw EOFException()
            if (!title.startsWith("HTTP/1.1 ") && !title.startsWith("HTTP/1.0 ")) {
                throw IOException("Unsupported HTTP version. Response: \"$title\"")
            }
            val responseCode = title.substring(9, 12).toInt()
            val headers = HashHeaders()
            while (true) {
                val str = channel.reader.readln() ?: throw EOFException()
                if (str.isEmpty()) {
                    break
                }
                val items = str.split(": ")
                headers.add(key = items[0], value = items[1])
            }

            return DefaultHttpResponse(
                url = url,
                client = client,
                keepAlive = keepAlive,
                channel = channel,
                responseCode = responseCode,
                headers = headers,
            )
        }
    }

    private var closed = false

    private fun checkClosed() {
        if (closed) {
            throw IllegalStateException("HttpResponse already closed")
        }
    }

    override suspend fun readData(): AsyncInput {
        val keepAlive = keepAlive
                && headers.getSingle(Headers.CONNECTION).equals(Headers.KEEP_ALIVE, ignoreCase = true)
        checkClosed()
        val encode = headers.getTransferEncoding()
        var stream: AsyncInput = channel.reader
        if (encode != null) {
            if (encode.toLowerCase() != Headers.CHUNKED.toLowerCase()) {
                throw IOException("Unknown Transfer Encoding \"$encode\"")
            }
            stream = ResponseAsyncChunkedInput(
                url = url,
                client = client,
                keepAlive = keepAlive,
                stream = stream,
                channel = channel,
            )
        } else {
            val len = headers.getContentLength() ?: throw IOException("Invalid Http Response: Unknown size of Response")
            stream = ResponseAsyncContentLengthInput(
                url = url,
                client = client,
                keepAlive = keepAlive,
                channel = channel,
                stream = stream,
                contentLength = len,
            )
        }

        closed = true
        return when (val encoding = headers.getContentEncoding()?.toLowerCase()) {
            "gzip" -> AsyncGZIPInput(stream, closeStream = true)
            "deflate" -> AsyncInflateInput(stream = stream, closeStream = true, wrap = true)
            null, "identity" -> stream
            else -> throw IOException("Unknown Content Encoding: \"$encoding\"")
        }
    }

    override suspend fun readText(): AsyncReader =
        readData().bufferedReader(
            charset = headers.getCharset() ?: Charsets.UTF8,
            closeParent = true
        )

    override suspend fun asyncClose() {
        checkClosed()
        try {
            channel.asyncClose()
        } finally {
            closed = true
        }
    }
}