package pw.binom.io.httpClient

import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncContentLengthOutput
import pw.binom.net.URL

class RequestAsyncContentLengthOutput constructor(
    val URI: URL,
    val client: BaseHttpClient,
    var keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    contentLength: ULong,
) : AsyncHttpRequestOutput, AsyncContentLengthOutput(
    stream = channel.writer,
    contentLength = contentLength,
    closeStream = false
) {
    override suspend fun getResponse(): HttpResponse {
        checkClosed()
        println("FINISH REQUEST $wrote/$contentLength")
        if (!isFull) {
            throw IllegalStateException("Not all content was sent")
        }
        super.asyncClose()
        return DefaultHttpResponse.read(
            uri = URI,
            client = client,
            keepAlive = keepAlive,
            channel = channel,
        )
    }

    override suspend fun asyncClose() {
        getResponse().asyncClose()
    }
}
