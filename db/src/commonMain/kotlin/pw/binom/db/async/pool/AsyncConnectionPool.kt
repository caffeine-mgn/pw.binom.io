package pw.binom.db.async.pool

import pw.binom.db.async.AsyncConnection
import pw.binom.io.AsyncCloseable
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

interface AsyncConnectionPool : AsyncCloseable {
    val idleConnectionCount: Int
    val connectionCount: Int
    suspend fun <T> borrow(func: suspend PooledAsyncConnection.() -> T): T

    companion object {
        @OptIn(ExperimentalTime::class)
        fun create(
            maxConnections: Int,
            pingTime: Duration = 1.0.minutes,
            idleTime: Duration = 5.0.minutes,
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