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
        val keepAlive = keepAlive && headers.keepAlive
        checkClosed()
        val encode = headers.transferEncoding
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
            val len = headers.contentLength
                ?: throw IOException("Invalid Http Response Headers: Unknown size of Response")
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
        return when (val encoding = headers.contentEncoding?.toLowerCase()) {
            "gzip" -> AsyncGZIPInput(stream, closeStream = true)
            "deflate" -> AsyncInflateInput(stream = stream, closeStream = true, wrap = true)
            null, "identity" -> stream
            else -> throw IOException("Unknown Content Encoding: \"$encoding\"")
        }
    }

    override suspend fun readText(): AsyncReader =
        readData().bufferedReader(
            charset = headers.charset?.let { Charsets.get(it) } ?: Charsets.UTF8,
            closeParent = true
        )

    override suspend fun asyncClose() {
        checkClosed()
        try {
            if (headers.bodyExist) {
                channel.asyncClose()
            } else {
                client.recycleConnection(
                    url = url,
                    channel = channel
                )
            }
        } finally {
            closed = true
        }
    }
}