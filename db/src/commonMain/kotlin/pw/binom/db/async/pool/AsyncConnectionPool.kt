package pw.binom.db.async.pool

import pw.binom.db.async.AsyncConnection
import pw.binom.io.AsyncCloseable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

interface AsyncConnectionPool : AsyncCloseable {
    val idleConnectionCount: Int
    val connectionCount: Int
    suspend fun <T> borrow(func: suspend PooledAsyncConnection.() -> T): T

    companion object {
        fun create(
            maxConnections: Int,
            pingTime: Duration = 1.minutes,
            idleTime: Duration = 5.minutes,
            waitFreeConnection: Boolean = true,
            factory: suspend () -> AsyncConnection,
        ) = AsyncConnectionPoolImpl(
            maxConnections = maxConnections,
            pingTime = pingTime,
            idleTime = idleTime,
            waitFreeConnection = waitFreeConnection,
            factory = factory,
        )
    }
}
