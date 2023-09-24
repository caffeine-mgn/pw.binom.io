package pw.binom.io.httpClient

import pw.binom.io.http.AsyncAsciiChannel
import pw.binom.io.httpClient.protocol.HttpConnect
import pw.binom.url.URL

interface ConnectionPoolReceiver {
    suspend fun recycle(url: URL, connection: HttpConnect)
    suspend fun close(connection: HttpConnect)
}
