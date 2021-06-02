package pw.binom.io.httpClient

import pw.binom.net.URI
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncContentLengthOutput
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RequestAsyncContentLengthOutput constructor(
    val URI: URI,
    val client: HttpClient,
    var keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    contentLength: ULong,
    val timeout: Duration?,
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
            uri = URI,
            client = client,
            keepAlive = keepAlive,
            channel = channel,
            timeout = timeout,
        )
    }

    override suspend fun asyncClose() {
        getResponse().asyncClose()
    }
}