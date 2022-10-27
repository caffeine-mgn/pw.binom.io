package pw.binom.io.httpClient

import pw.binom.io.*
import pw.binom.io.http.Headers

interface HttpResponse : AsyncCloseable {
    val responseCode: Int
    val headers: Headers
    suspend fun readData(): AsyncInput
    suspend fun readText(): AsyncReader
    suspend fun startTcp(): AsyncChannel
    suspend fun <T> readText(func: suspend (AsyncReader) -> T): T = readText().use { func(it) }
    suspend fun <T> readData(func: suspend (AsyncInput) -> T): T = readData().use { func(it) }
}
