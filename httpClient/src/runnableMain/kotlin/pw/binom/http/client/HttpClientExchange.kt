package pw.binom.http.client

import pw.binom.charset.Charsets
import pw.binom.copyTo
import pw.binom.io.*
import pw.binom.io.http.Headers

interface HttpClientExchange : AsyncCloseable {
  val isEof: Boolean
  fun getOutput(): AsyncOutput
  suspend fun getInput(): AsyncInput
  suspend fun getResponseHeaders(): Headers
  suspend fun getResponseCode(): Int
  suspend fun reader(): AsyncReader {
    val charset = getResponseHeaders().charset?.let { Charsets.get(it) } ?: Charsets.UTF8
    return getInput().bufferedReader(charset = charset)
  }

  suspend fun readAllText(): String = reader().useAsync {
    it.readText()
  }

  suspend fun readBytes() = ByteArrayOutput().use { out ->
    getInput().copyTo(out)
    out.toByteArray()
  }
}
