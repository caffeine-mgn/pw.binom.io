package pw.binom.io.httpServer

import pw.binom.ByteBufferPool
import pw.binom.io.http.AsyncMultipartInput

fun HttpRequest.multipart(bufferPool: ByteBufferPool): AsyncMultipartInput? {
    val contentType = headers.contentType ?: return null
    if (!contentType.startsWith("multipart/form-data;")) {
        return null
    }

    val boundary = contentType.substring(contentType.indexOf(';') + 1).trim().removePrefix("boundary=")
    return AsyncMultipartInput(
        separator = boundary,
        bufferPool = bufferPool,
        stream = readBinary(),
    )
}

fun HttpServerExchange.multipart(bufferPool: ByteBufferPool): AsyncMultipartInput? {
    val contentType = requestHeaders.contentType ?: return null
    if (!contentType.startsWith("multipart/form-data;")) {
        return null
    }

    val boundary = contentType.substring(contentType.indexOf(';') + 1).trim().removePrefix("boundary=")
    return AsyncMultipartInput(
        separator = boundary,
        bufferPool = bufferPool,
        stream = input,
    )
}
