package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.URL
import pw.binom.io.http.AsyncChunkedInput

class ResponseAsyncChunkedInput(
    val url: URL,
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
                url = url,
                channel = channel,
            )
        } else {
            channel.asyncClose()
        }
    }
}