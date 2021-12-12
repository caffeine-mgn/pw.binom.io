package pw.binom.io.httpClient

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import pw.binom.AsyncInput
import pw.binom.CancelledException
import pw.binom.EmptyAsyncInput
import pw.binom.TimeoutException
import pw.binom.charset.Charsets
import pw.binom.compression.zlib.AsyncGZIPInput
import pw.binom.compression.zlib.AsyncInflateInput
import pw.binom.io.*
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.Encoding
import pw.binom.io.http.HashHeaders
import pw.binom.io.http.Headers
import pw.binom.net.URI
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class DefaultHttpResponse(
    val URI: URI,
    val client: BaseHttpClient,
    var keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    override val responseCode: Int,
    override val headers: Headers,
) : HttpResponse {
    companion object {
        @OptIn(ExperimentalTime::class)
        suspend fun read(
            uri: URI,
            client: BaseHttpClient,
            keepAlive: Boolean,
            channel: AsyncAsciiChannel,
            timeout: Duration?,
        ): HttpResponse {
            var headerReadFlag = false
            if (timeout != null) {
                withContext(client.networkDispatcher) {
                    try {
                        delay(timeout)
                    } finally {
                        if (!headerReadFlag) {
                            client.interruptAndClose(channel)
                        }
                    }
                }
            }
            try {
                val title = try {
                    channel.reader.readln() ?: throw EOFException()
                } catch (e: ClosedException) {
                    if (!headerReadFlag) {
                        throw CancellationException("Header read timeout $timeout")
                    } else {
                        throw e
                    }
                }
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
                    URI = uri,
                    client = client,
                    keepAlive = keepAlive,
                    channel = channel,
                    responseCode = responseCode,
                    headers = headers,
                )
            } catch (e: CancelledException) {
                throw TimeoutException("Can't get headers from request to $uri")
            } finally {
                headerReadFlag = true
            }
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
        if (responseCode == 204) {
            return EmptyAsyncInput
        }
        val transferEncoding = headers.getTransferEncodingList()
        val contentEncoding = headers.getContentEncodingList()
        val contentLength = headers.contentLength
        var stream: AsyncInput = channel.reader
        if (contentLength != null) {
            stream = ResponseAsyncContentLengthInput(
                URI = URI,
                client = client,
                keepAlive = keepAlive,
                channel = channel,
                stream = stream,
                contentLength = contentLength,
            )
        }

        fun wrap(encode: String, stream: AsyncInput) =
            when (encode) {
                Encoding.CHUNKED -> ResponseAsyncChunkedInput(
                    URI = URI,
                    client = client,
                    keepAlive = keepAlive,
                    stream = stream,
                    channel = channel,
                )
                Encoding.GZIP -> AsyncGZIPInput(stream, closeStream = true)
                Encoding.DEFLATE -> AsyncInflateInput(stream = stream, closeStream = true, wrap = true)
                Encoding.IDENTITY -> stream
                else -> null
            }
        for (i in transferEncoding.lastIndex downTo 0) {
            stream = wrap(encode = transferEncoding[i], stream = stream)
                ?: throw IOException("Unknown Content Encoding: \"${transferEncoding[i]}\"")
        }
        for (i in contentEncoding.lastIndex downTo 0) {
            stream = wrap(encode = contentEncoding[i], stream = stream)
                ?: throw IOException("Unknown Content Encoding: \"${contentEncoding[i]}\"")
        }
        closed = true
        return stream
    }

    override suspend fun readText(): AsyncReader =
        readData().bufferedReader(
            charset = headers.charset?.let { Charsets.get(it) } ?: Charsets.UTF8,
            closeParent = true,
//            bufferSize = client.bufferSize,
            pool = client.textBufferPool,
        )

    override suspend fun startTcp(): AsyncChannel {
        closed = true
        return channel.channel
    }

    override suspend fun asyncClose() {
        if (closed) {
            return
        }
        try {
            if (headers.bodyExist) {
                client.interruptAndClose(channel)
            } else {
                client.recycleConnection(
                    URI = URI,
                    channel = channel
                )
            }
        } finally {
            closed = true
        }
    }
}