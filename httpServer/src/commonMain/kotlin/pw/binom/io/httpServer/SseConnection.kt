package pw.binom.io.httpServer

import pw.binom.io.AsyncCloseable
import pw.binom.io.AsyncReader

interface SseConnection : AsyncCloseable {
  suspend fun send(data: String?, eventName: String? = null, id: String? = null, retry: String? = null)
  suspend fun send(data: AsyncReader, eventName: String? = null, id: String? = null, retry: String? = null)
}
