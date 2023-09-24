package pw.binom.io.httpClient

import pw.binom.io.AsyncInput
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncContentLengthInput
import pw.binom.url.URL
/*
class ResponseAsyncContentLengthInput(
    val URI: URL,
    val keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    stream: AsyncInput,
    contentLength: ULong,
    val connectionPoolReceiver: ConnectionPoolReceiver?,
) : AsyncContentLengthInput(
    stream = stream,
    contentLength = contentLength,
    closeStream = false,
) {

    override suspend fun asyncClose() {
        if (!isEof) {
            skipAll()
        }
        super.asyncClose()
        if (keepAlive) {
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
*/
