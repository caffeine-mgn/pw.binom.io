package pw.binom.io.httpServer

import pw.binom.io.AsyncInputStream
import pw.binom.io.socket.ConnectionManager

class HttpRequestImpl(
        private val connection: ConnectionManager.Connection,
        override val method: String,
        override val uri: String,
        override val headers: Map<String, List<String>>) : HttpRequest {

    override val contextUri = uri

    override val input = object : AsyncInputStream {
        var readed = 0L
        override fun close() {
        }

        override suspend fun read(data: ByteArray, offset: Int, length: Int): Int {
            val size = size
            var localLength = length
            if (size != null && size > 0L) {
                if (readed + localLength.toLong() > size) {
                    localLength = (size - readed).toInt()
                }
            }
            if (localLength == 0)
                return 0
            val r = connection.input.read(data, offset, localLength)
            readed += r
            return r
        }

    }
    private val size by lazy {
        headers["Content-Length"]?.singleOrNull()?.toLongOrNull()?.let { it }
    }
}

private class PrivateHttpRequestImpl(
        override val headers: Map<String, List<String>>,
        override val input: AsyncInputStream,
        override val method: String,
        override val uri: String,
        override val contextUri: String
) : HttpRequest

fun HttpRequest.withContextURI(contextURI: String): HttpRequest =
        PrivateHttpRequestImpl(
                headers = headers,
                input = input,
                method = method,
                uri = uri,
                contextUri = contextURI
        )

fun HttpRequest.withMethod(method: String): HttpRequest =
        PrivateHttpRequestImpl(
                headers = headers,
                input = input,
                method = method,
                uri = uri,
                contextUri = contextUri
        )