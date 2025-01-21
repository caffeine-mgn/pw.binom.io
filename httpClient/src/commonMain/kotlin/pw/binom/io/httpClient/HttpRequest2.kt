package pw.binom.io.httpClient

import pw.binom.io.AsyncChannel
import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncOutput

interface HttpRequest2 : AsyncCloseable {
  val closed: Boolean
  suspend fun getOutput(): AsyncOutput
  suspend fun getChannel(): AsyncChannel
  suspend fun switchToResponse(): HttpResponse2
}
