package pw.binom.io.httpClient

import pw.binom.AsyncOutput
import pw.binom.ByteBuffer
import pw.binom.compression.zlib.AsyncDeflaterOutput
import pw.binom.compression.zlib.AsyncGZIPOutput
import pw.binom.io.IOException
import pw.binom.io.http.AsyncChunkedOutput
import pw.binom.io.http.AsyncContentLengthOutput
import pw.binom.io.http.Headers

internal class UrlRequestImpl(
        val headers: Map<String, List<String>>,
        val channel: AsyncHttpClient.Connection,
        val connection: UrlConnectImpl,
        flushBuffer: Int,
        val compressLevel: Int,
        val compressBufferSize: Int
) : AsyncHttpClient.UrlRequest {
    private var dataSent = false
    private val httpStream: AsyncOutput = run {
        val encode = headers[Headers.TRANSFER_ENCODING]
        var stream: AsyncOutput = channel.channel

        val contentLength = headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull()
        if (contentLength != null)
            stream = AsyncContentLengthOutput(
                    stream = stream,
                    contentLength = contentLength,
                    closeStream = false
            )

        if (encode != null) {
            encode
                    .asSequence()
                    .flatMap { it.splitToSequence(',') }
                    .map { it.trim().toLowerCase() }
                    .forEach {
                        stream = when (it) {
                            "chunked" -> AsyncChunkedOutput(
                                    stream = stream,
                                    closeStream = stream != channel.channel,
                                    autoFlushBuffer = flushBuffer
                            )
                            "gzip" -> AsyncGZIPOutput(
                                    stream = stream,
                                    closeStream = stream != channel.channel,
                                    bufferSize = compressBufferSize,
                                    level = compressLevel
                            )
                            "deflate" -> AsyncDeflaterOutput(
                                    stream = stream,
                                    wrap = true,
                                    closeStream = stream != channel.channel,
                                    bufferSize = compressBufferSize,
                                    level = compressLevel
                            )
                            "identity" -> stream
                            else -> stream
                        }
                    }
        }
//        if (headers[Headers.TRANSFER_ENCODING]?.singleOrNull()?.split(',')?.map { it.trim() }?.any { it == Headers.CHUNKED } == true)
//            return@run AsyncChunkedOutput(
//                    stream = channel.channel,
//                    autoCloseStream = false,
//                    autoFlushBuffer = flushBuffer
//            )


        stream
    }

    private val stream = run {
        val contentEncode = headers[Headers.CONTENT_ENCODING]?.lastOrNull()?.toLowerCase()

        when (contentEncode) {
            "gzip" -> AsyncGZIPOutput(httpStream, closeStream = true, level = compressLevel, bufferSize = compressBufferSize)
            "deflate" -> AsyncDeflaterOutput(httpStream, wrap = true, closeStream = true, bufferSize = compressBufferSize, level = compressLevel)
            "identity", null -> httpStream
            else -> throw IOException("Unknown Encoding: \"$contentEncode\"")
        }
    }

    private val ff
        get() = stream


    override suspend fun response(): AsyncHttpClient.UrlResponse {
        dataSent = true
        ff.asyncClose()

//        channel.channel.writeByte(0x0D)
//        channel.channel.writeByte(0x0A)
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
        return ff.write(data)
    }

    override suspend fun flush() {
        checkSent()
        ff.flush()
    }

    override suspend fun asyncClose() {
        checkSent()
        ff.asyncClose()
        channel.asyncClose()
    }

}