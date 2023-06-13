package pw.binom.io.httpClient

import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.asyncOutput
import pw.binom.charset.Charsets
import pw.binom.copyTo
import pw.binom.io.*
import pw.binom.io.http.Headers

interface HttpResponse : AsyncCloseable {
    val responseCode: Int
    val headers: Headers
    suspend fun readData(): AsyncInput
    suspend fun readData(output: AsyncOutput, bufferSize: Int = DEFAULT_BUFFER_SIZE) = readData { input ->
        input.copyTo(dest = output, bufferSize = bufferSize)
    }

    suspend fun readDataToByteArray(bufferSize: Int = DEFAULT_BUFFER_SIZE) = ByteArrayOutput().use { output ->
        readData(output.asyncOutput(), bufferSize = bufferSize)
        output.toByteArray()
    }

    suspend fun readText(): AsyncReader = readData().bufferedReader(
        charset = headers.charset?.let { Charsets.get(it) } ?: Charsets.UTF8,
    )

//    suspend fun startTcp(): AsyncChannel
    suspend fun <T> readText(func: suspend (AsyncReader) -> T): T = readText().use { func(it) }
    suspend fun <T> readData(func: suspend (AsyncInput) -> T): T = readData().use { func(it) }
}
