package pw.binom.io.httpClient
/*
import pw.binom.ByteBufferPool
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncContentLengthOutput
import pw.binom.url.URL

class RequestAsyncContentLengthOutput constructor(
    val URI: URL,
    val client: BaseHttpClient,
    var keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    contentLength: ULong,
    val connectionPoolReceiver: ConnectionPoolReceiver?,
    val textBufferPool: ByteBufferPool,
) : AsyncHttpRequestOutput, AsyncContentLengthOutput(
    stream = channel.writer,
    contentLength = contentLength,
    closeStream = false
) {
    override suspend fun getResponse(): HttpResponse {
        ensureOpen()
        if (!isFull) {
            throw IllegalStateException("Not all content was sent")
        }
        super.asyncClose()
        return DefaultHttpResponse.read(
            uri = URI,


            keepAlive = keepAlive,
            channel = channel,
            connectionPoolReceiver = connectionPoolReceiver,
            textBufferPool = textBufferPool,
        )
    }

    override suspend fun asyncClose() {
        getResponse().asyncClose()
    }
}
*/
