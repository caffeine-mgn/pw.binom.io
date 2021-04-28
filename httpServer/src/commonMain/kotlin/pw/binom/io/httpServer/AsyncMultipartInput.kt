package pw.binom.io.httpServer

import pw.binom.ByteBufferAllocator
import pw.binom.DEFAULT_BYTEBUFFER_ALLOCATOR
import pw.binom.io.http.AsyncMultipartInput

fun HttpRequest.multipart(bufferPool: ByteBufferAllocator = DEFAULT_BYTEBUFFER_ALLOCATOR): AsyncMultipartInput? {
    val contentType = headers.contentType ?: return null
    if (!contentType.startsWith("multipart/form-data;"))
        return null

    val boundary = contentType.substring(contentType.indexOf(';') + 1).trim().removePrefix("boundary=")
    return AsyncMultipartInput(
        separator = boundary,
        bufferPool = bufferPool,
        stream = readBinary()
    )
}


//fun HttpRequestDeprecated.multipart(bufferPool: ObjectPool<ByteBuffer>): AsyncMultipartInput? {
//    val contentType = headers[Headers.CONTENT_TYPE]?.singleOrNull() ?: return null
//    if (!contentType.startsWith("multipart/form-data;"))
//        return null
//
//    val boundary = contentType.substring(contentType.indexOf(';') + 1).trim().removePrefix("boundary=")
//    return AsyncMultipartInput(
//        separator = boundary,
//        bufferPool = bufferPool,
//        stream = input
//    )
//}

