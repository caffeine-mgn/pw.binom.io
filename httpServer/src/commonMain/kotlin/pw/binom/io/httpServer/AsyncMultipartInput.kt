package pw.binom.io.httpServer

import pw.binom.ByteBuffer
import pw.binom.io.http.AsyncMultipartInput
import pw.binom.io.http.Headers
import pw.binom.pool.ObjectPool



fun HttpRequestDeprecated.multipart(bufferPool: ObjectPool<ByteBuffer>): AsyncMultipartInput? {
    val contentType = headers[Headers.CONTENT_TYPE]?.singleOrNull() ?: return null
    if (!contentType.startsWith("multipart/form-data;"))
        return null

    val boundary = contentType.substring(contentType.indexOf(';') + 1).trim().removePrefix("boundary=")
    return AsyncMultipartInput(
            separator = boundary,
            bufferPool = bufferPool,
            stream = input
    )
}

