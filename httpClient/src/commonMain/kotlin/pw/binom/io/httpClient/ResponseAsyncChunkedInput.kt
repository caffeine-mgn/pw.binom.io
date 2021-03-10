package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.URI
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncChunkedInput

class ResponseAsyncChunkedInput(
    val URI: URI,
    val client: HttpClient,
    val keepAlive: Boolean,
    val channel: AsyncAsciiChannel,
    stream: AsyncInput,
) : AsyncChunkedInput(
    stream = stream,
    closeStream = false
) {

    override suspend fun asyncClose() {
        val eof = isEof
        super.asyncClose()
        if (keepAlive && eof) {
            client.recycleConnection(
                URI = URI,
                channel = channel,
            )
        } else {
            channel.asyncClose()
        }
    }
}