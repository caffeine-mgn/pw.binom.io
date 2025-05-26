package pw.binom.io.httpServer

import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncReader

interface SseConnection:AsyncCloseable {
  suspend fun send(event: String?, data: String)
  suspend fun send(event: String?, data: AsyncReader)
}
