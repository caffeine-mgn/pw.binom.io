package pw.binom.io.httpClient

import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncReader
import pw.binom.io.http.Headers

interface HttpResponse : AsyncCloseable {
    val responseCode: Int
    val headers: Headers
    suspend fun readData(): AsyncInput
    suspend fun readText(): AsyncReader
    suspend fun startTcp(): AsyncChannel
}
