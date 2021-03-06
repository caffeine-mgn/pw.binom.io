package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.URL
import pw.binom.io.http.AsyncContentLengthInput

class ResponseAsyncContentLengthInput(
    val url: URL,
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
                url = url,
                channel = channel,
            )
        } else {
            channel.asyncClose()
        }
    }
}