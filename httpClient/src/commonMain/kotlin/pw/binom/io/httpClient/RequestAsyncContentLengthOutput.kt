package pw.binom.io.httpClient

import pw.binom.URL
import pw.binom.io.http.AsyncContentLengthOutput

class RequestAsyncContentLengthOutput(
    val url: URL,
    val client: HttpClient,
    var keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    contentLength: ULong
) : AsyncHttpRequestOutput, AsyncContentLengthOutput(
    stream = channel.writer,
    contentLength = contentLength,
    closeStream = false
) {
    override suspend fun getResponse(): HttpResponse {
        checkClosed()
        if (!isFull) {
            throw IllegalStateException("Not all content was sent")
        }
        super.asyncClose()
        return DefaultHttpResponse.read(
            url = url,
            client = client,
            keepAlive = keepAlive,
            channel = channel,
        )
    }

    override suspend fun asyncClose() {
        getResponse().asyncClose()
    }
}