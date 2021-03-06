package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.URL
import pw.binom.io.http.AsyncChunkedOutput

class RequestAsyncChunkedOutput(
    val url: URL,
    val client: HttpClient,
    var keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    autoFlushBuffer: Int = DEFAULT_BUFFER_SIZE,
) : AsyncHttpRequestOutput, AsyncChunkedOutput(
    stream = channel.writer,
    autoFlushBuffer = autoFlushBuffer,
    closeStream = true
) {

    override suspend fun getResponse(): HttpResponse {
        checkClosed()
        super.asyncClose()
        return DefaultHttpResponse.read(
            url = url,
            client = client,
            keepAlive = keepAlive,
            channel = channel,
        )
    }

    override suspend fun asyncClose() {
        checkClosed()
        super.asyncClose()
        stream.asyncClose()
    }
}