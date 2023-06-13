package pw.binom.io.httpClient

import pw.binom.ByteBufferPool
import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.http.Headers
import pw.binom.url.URL
/*
class DefaultHttpRequestBody(
    override val headers: Headers,
    override val autoFlushBuffer: Int,
    val connectionPoolReceiver: ConnectionPoolReceiver,
    val textBufferPool: ByteBufferPool,
    val url: URL, override val input: AsyncInput, override val output: AsyncOutput,
) : AbstractHttpRequestBody() {
    override suspend fun flush(): HttpResponse {
        internalIsFlushed = true
        output.flush()
        return DefaultHttpResponse.read(
            uri = url,
            connectionPoolReceiver = connectionPoolReceiver,
            keepAlive = headers.keepAlive,
            channel = channel,
            textBufferPool = textBufferPool,
        )
    }
}
*/
