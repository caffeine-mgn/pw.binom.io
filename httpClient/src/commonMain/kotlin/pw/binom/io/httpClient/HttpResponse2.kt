package pw.binom.io.httpClient

import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncOutput

interface HttpResponse2:AsyncCloseable {
  val responseCode: Int
  val closed: Boolean
  suspend fun getOutput(): AsyncOutput
  suspend fun getChannel(): AsyncChannel
}
