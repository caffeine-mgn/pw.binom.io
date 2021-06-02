package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.net.URI
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncChunkedOutput
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class RequestAsyncChunkedOutput constructor(
    val URI: URI,
    val client: HttpClient,
    var keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    val timeout: Duration?,
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