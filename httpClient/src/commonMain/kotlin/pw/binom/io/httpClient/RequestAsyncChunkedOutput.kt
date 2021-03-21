package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.net.URI
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncChunkedOutput

class RequestAsyncChunkedOutput(
    val URI: URI,
    val client: HttpClient,
    var keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE,
) : AsyncHttpRequestOutput, AsyncChunkedOutput(
    stream = channel.writer,
    autoFlushBuffer = autoFlushBuffer,
    closeStream = false
) {

    override suspend fun getResponse(): HttpResponse {
        checkClosed()
        super.asyncClose()
        return DefaultHttpResponse.read(
            URI = URI,
            client = client,
            keepAlive = keepAlive,
            channel = channel,
        )
    }

    override suspend fun asyncClose() {
        getResponse().asyncClose()
    }
}