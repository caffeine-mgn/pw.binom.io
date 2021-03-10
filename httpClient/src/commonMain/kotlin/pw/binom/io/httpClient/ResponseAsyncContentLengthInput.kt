package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.URI
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncContentLengthInput

class ResponseAsyncContentLengthInput(
    val URI: URI,
    val client: HttpClient,
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