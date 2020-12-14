package pw.binom.io.httpServer

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.empty
import pw.binom.io.AbstractAsyncBufferedInput
import pw.binom.io.IOException
import pw.binom.io.http.AsyncMultipartInput
import pw.binom.io.http.Headers
import pw.binom.io.readln
import pw.binom.io.utf8Reader
import pw.binom.pool.ObjectPool



fun HttpRequest.multipart(bufferPool: ObjectPool<ByteBuffer>): AsyncMultipartInput? {
    val contentType = headers[Headers.CONTENT_TYPE]?.singleOrNull() ?: return null
    if (contentType.startsWith("multipart/form-data;") != true)
        return null

    val boundary = contentType.substring(contentType.indexOf(';') + 1).trim().removePrefix("boundary=")
    return AsyncMultipartInput(
            separator = boundary,
            bufferPool = bufferPool,
            stream = input
    )
}

