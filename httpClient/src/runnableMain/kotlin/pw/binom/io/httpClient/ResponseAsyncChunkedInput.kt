package pw.binom.io.httpClient
/*
import pw.binom.io.AsyncInput
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncChunkedInput
import pw.binom.url.URL

class ResponseAsyncChunkedInput(
    val URI: URL,
    val keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    stream: AsyncInput,
    val connectionPoolReceiver: ConnectionPoolReceiver?,
) : AsyncChunkedInput(
    stream = stream,
    closeStream = false,
) {

    override suspend fun asyncClose() {
        var ok = false
        try {
            if (!isEof) {
                skipAll()
            }
            super.asyncClose()
            ok = true
        } finally {
            if (ok && keepAlive) {
                if (connectionPoolReceiver != null) {
                    connectionPoolReceiver.recycle(
                        url = URI,
                        connection = channel,
                    )
                } else {
                    channel.asyncClose()
                }
            } else {
                if (connectionPoolReceiver != null) {
                    connectionPoolReceiver.close(channel)
                } else {
                    channel.asyncClose()
                }
            }
        }
    }
}
*/
