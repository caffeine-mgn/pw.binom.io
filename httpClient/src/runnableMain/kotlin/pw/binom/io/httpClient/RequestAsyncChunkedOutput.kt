package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncChunkedOutput
import pw.binom.url.URL
/*
class RequestAsyncChunkedOutput constructor(
    val URI: URL,
    val client: BaseHttpClient,
    var keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE,
) : AsyncHttpRequestOutput, AsyncChunkedOutput(
    stream = channel.writer,
    autoFlushBuffer = autoFlushBuffer,
    closeStream = false
) {

    override suspend fun getResponse(): HttpResponse {
        ensureOpen()
        super.asyncClose()
        return DefaultHttpResponse.read(
            uri = URI,
            keepAlive = keepAlive,
            channel = channel,
            connectionPoolReceiver = client.connectionPoolReceiver,
            textBufferPool = client.textBufferPool,
        )
    }

    override suspend fun asyncClose() {
        getResponse().asyncClose()
    }
}
*/