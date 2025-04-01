package pw.binom.http.client

import pw.binom.charset.Charsets
import pw.binom.copyTo
import pw.binom.io.*
import pw.binom.io.http.Headers

interface HttpClientExchange : HttpClientCommonExchange {
  val isEof: Boolean
  fun getOutput(): AsyncOutput
  suspend fun getInput(): AsyncInput
  override suspend fun getResponseHeaders(): Headers
  override suspend fun getResponseCode(): Int
  override suspend fun sendText(text: String) {
    getOutput().bufferedWriter().useAsync { out ->
      out.append(text)
    }
  }

  suspend fun reader(): AsyncReader {
    val charset = getResponseHeaders().charset?.let { Charsets.get(it) } ?: Charsets.UTF8
    return getInput().bufferedReader(charset = charset)
  }

  override suspend fun readAllText(): String = reader().useAsync {
    it.readText()
  }

  override suspend fun readAllBytes() = ByteArrayOutput().use { out ->
    getInput().copyTo(out)
    out.toByteArray()
  }
}
