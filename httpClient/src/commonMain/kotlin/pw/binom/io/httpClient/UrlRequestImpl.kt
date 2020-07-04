package pw.binom.io.httpClient

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.io.http.*

internal class UrlRequestImpl(
        val headers: Map<String, List<String>>,
        val channel: AsyncHttpClient.Connection,
        val connection:UrlConnectImpl,
        flushBuffer: Int
) : AsyncHttpClient.UrlRequest {
    private var dataSent = false
    private val stream: AsyncOutput = run {
        if (headers[Headers.TRANSFER_ENCODING]?.singleOrNull() == Headers.CHUNKED)
            return@run AsyncChunkedOutput(
                    stream = channel.channel,
                    autoCloseStream = false,
                    autoFlushBuffer = flushBuffer
            )

        val contentLength = headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull()
        if (contentLength != null)
            return@run AsyncContentLengthOutput(
                    stream = channel.channel,
                    contentLength = contentLength
            )
        TODO("Unknown Encode")
    }

    override suspend fun response(): AsyncHttpClient.UrlResponse {
        dataSent = true
        stream.close()
        return connection.compliteRequest()
    }

    private fun checkSent() {
        if (dataSent)
            throw IllegalStateException("Data already sent")
    }

    override suspend fun write(data: ByteBuffer): Int {
        checkSent()
        if (data.remaining <= 0) {
            return 0
        }
        return stream.write(data)
    }

    override suspend fun flush() {
        checkSent()
        stream.flush()
    }

    override suspend fun close() {
        checkSent()
        stream.close()
        channel.close()
    }

}