package pw.binom.http.client

import pw.binom.io.AsyncCloseable
import pw.binom.io.http.Headers

interface SseConnection : AsyncCloseable {
  val responseCode: Int
  val responseHeaders: Headers
  suspend fun <T> read(read: (String?, String) -> T): T
}
