package pw.binom.io.httpServer

import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncReader

interface SseConnection : AsyncCloseable {
  suspend fun send(eventName: String?, id: String, retry: String?, data: String)
  suspend fun send(eventName: String?, id: String, retry: String?, data: AsyncReader)
}
