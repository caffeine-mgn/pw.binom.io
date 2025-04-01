package pw.binom.http.client

import pw.binom.io.AsyncCloseable
import pw.binom.io.http.Headers

interface HttpClientCommonExchange : AsyncCloseable {
  suspend fun getResponseHeaders(): Headers
  suspend fun getResponseCode(): Int
  suspend fun readAllText(): String
  suspend fun readAllBytes():ByteArray
  suspend fun sendText(text: String)
}
