package pw.binom.io.httpClient

import pw.binom.io.Closeable
import pw.binom.net.URI
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

interface HttpClient: Closeable {
    @OptIn(ExperimentalTime::class)
    suspend fun connect(method: String, uri: URI, timeout: Duration? = null): HttpRequest
}