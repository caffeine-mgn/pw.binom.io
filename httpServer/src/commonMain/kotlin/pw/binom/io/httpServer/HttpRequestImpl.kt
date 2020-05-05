@file:UseExperimental(ExperimentalUnsignedTypes::class)

package pw.binom.io.httpServer

import pw.binom.io.AsyncEmptyInputStream
import pw.binom.io.AsyncInputStream
import pw.binom.io.LazyAsyncInputStream
import pw.binom.io.http.AsyncChunkedInputStream
import pw.binom.io.http.AsyncContentLengthInputStream
import pw.binom.io.http.Headers
import pw.binom.io.socket.ConnectionManager

class HttpRequestImpl(
        private val connection: ConnectionManager.ConnectionRaw,
        override val method: String,
        override val uri: String,
        override val headers: Map<String, List<String>>) : HttpRequest {

    override val contextUri = uri


    override val input = LazyAsyncInputStream {
        when {
            headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.toULongOrNull() != null ->
                AsyncContentLengthInputStream(connection.input, headers[Headers.CONTENT_LENGTH]!!.single().toULong())
            headers[Headers.TRANSFER_ENCODING]?.any { it == Headers.CHUNKED } == true -> {
                AsyncChunkedInputStream(connection.input)
            }
            else -> AsyncEmptyInputStream
        }
    }

    private val size by lazy {
        headers[Headers.CONTENT_LENGTH]?.singleOrNull()?.toLongOrNull()?.let { it }
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