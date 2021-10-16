package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.net.URI
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncContentLengthInput
import pw.binom.skipAll

class ResponseAsyncContentLengthInput(
    val URI: URI,
    val client: BaseHttpClient,
    val keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    stream: AsyncInput,
    contentLength: ULong,
) : AsyncContentLengthInput(
    stream = stream,
    contentLength = contentLength,
    closeStream = false
) {

    override suspend fun asyncClose() {
        if(!isEof){
            skipAll()
        }
        super.asyncClose()
        if (keepAlive) {
            client.recycleConnection(
                URI = URI,
                channel = channel,
            )
        } else {
            channel.asyncClose()
        }
    }
}