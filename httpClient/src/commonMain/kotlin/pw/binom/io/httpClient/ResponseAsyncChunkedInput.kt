package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.net.URI
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncChunkedInput
import pw.binom.skipAll

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
        var ok = false
        try {
            if (!isEof) {
                skipAll()
            }
            super.asyncClose()
            ok = true
        } finally {
            if (ok && keepAlive) {
                client.recycleConnection(
                    URI = URI,
                    channel = channel,
                )
            } else {
                channel.asyncClose()
            }
        }
    }
}