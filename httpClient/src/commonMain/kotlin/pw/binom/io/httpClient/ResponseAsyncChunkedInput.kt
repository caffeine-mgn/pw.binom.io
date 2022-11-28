package pw.binom.io.httpClient

import pw.binom.io.AsyncInput
import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.http.AsyncChunkedInput
import pw.binom.skipAll
import pw.binom.url.URL

class ResponseAsyncChunkedInput(
    val URI: URL,
    val client: BaseHttpClient,
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
