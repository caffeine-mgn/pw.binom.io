package pw.binom.io.httpClient.protocol

import pw.binom.io.AsyncCloseable
import pw.binom.io.http.Headers
import pw.binom.io.httpClient.HttpRequestBody
import pw.binom.io.httpClient.HttpResponse
import pw.binom.url.URL
import kotlin.time.Duration

interface HttpConnect : AsyncCloseable {
    val isAlive: Boolean
    val age: Duration
    suspend fun makeGetRequest(pool: ConnectionPoll, method: String, url: URL, headers: Headers): HttpResponse =
        makePostRequest(
            method = method,
            url = url,
            headers = headers,
            pool = pool,
        ).flush()

    suspend fun makePostRequest(pool: ConnectionPoll, method: String, url: URL, headers: Headers): HttpRequestBody
}
