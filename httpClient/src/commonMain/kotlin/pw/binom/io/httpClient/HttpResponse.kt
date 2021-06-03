package pw.binom.io.httpClient

import pw.binom.AsyncInput
import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncReader
import pw.binom.io.http.Headers

interface HttpResponse : AsyncCloseable {
    val responseCode: Int
    val headers: Headers
    suspend fun readData(): AsyncInput
    suspend fun readText(): AsyncReader
}