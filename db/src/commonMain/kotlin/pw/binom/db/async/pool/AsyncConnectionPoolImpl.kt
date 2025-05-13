package pw.binom.db.async.pool

import pw.binom.db.async.AsyncConnection
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class AsyncConnectionPoolImpl (
  override val maxConnections: Int,
  override val pingTime: Duration = 1.0.minutes,
  override val idleTime: Duration = 5.0.minutes,
  override val waitFreeConnection: Boolean = true,
  val factory: suspend () -> AsyncConnection,
) : AbstractAsyncConnectionPool() {
  init {
    require(maxConnections >= 1) { "maxConnections should be grate than 0" }
  }

  override suspend fun createConnection(): AsyncConnection = factory()

}
