package pw.binom.db.pool

import pw.binom.date.Date
import pw.binom.db.AsyncConnection
import pw.binom.io.AsyncCloseable
import pw.binom.io.StreamClosedException
import pw.binom.io.use
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes

@OptIn(ExperimentalTime::class)
class AsyncConnectionPool constructor(
    val maxConnections: Int,
    val pingTime: Duration = 1.0.minutes,
    val idleTime: Duration = 5.0.minutes,
    val waitFreeConnection: Boolean = true,
    val factory: suspend () -> AsyncConnection,
) : AsyncCloseable {
    private val connections = HashSet<PooledAsyncConnectionImpl>(maxConnections)
    private val idleConnection = ArrayList<PooledAsyncConnectionImpl>(maxConnections)

    private val waiters = ArrayList<Continuation<PooledAsyncConnectionImpl>>()

    val idleConnectionCount
        get() = idleConnection.size

    val connectionCount
        get() = connections.size

    /**
     * Set for connection for delete
     */
    private val forRemove = HashSet<PooledAsyncConnectionImpl>()
    private var cleaning = false

    private fun getOneWater(): Continuation<PooledAsyncConnectionImpl>? =
        waiters.removeLastOrNull()

    fun prepareStatement(sql: String) {
        PooledAsyncPreparedStatement2(this, sql)
    }

    suspend fun cleanUp(): Int {
        if (cleaning) {
            return 0
        }
        var count = 0
        try {
            cleaning = true
            val it = idleConnection.iterator()
            while (it.hasNext()) {
                val e = it.next()
                if (Date.now - e.lastActive > idleTime.inMilliseconds.toLong()) {
                    it.remove()
                    connections -= e
                    forRemove += e
                    count++
                    continue
                }
            }
            forRemove.forEach {
                runCatching { it.asyncClose() }
            }
            forRemove.clear()
        } finally {
            cleaning = false
        }
        return count
    }

    private suspend fun getConnectionAnyWay(): PooledAsyncConnectionImpl {
        while (true) {
            val connection = idleConnection.removeLastOrNull()
            if (connection != null) {
                if (!connection.checkValid()) {
                    connections -= connection
                    forRemove += connection
                    continue
                }
                return connection
            }

            if (connections.size < maxConnections) {
                val con = PooledAsyncConnectionImpl(this, factory())
                connections.add(con)
                return con
            }
            if (!waitFreeConnection) {
                throw IllegalStateException("No free connections")
            }

            val con = suspendCoroutine<PooledAsyncConnectionImpl> { waiters += it }
            if (!con.checkValid()) {
                connections -= con
                forRemove += con
                continue
            }
            con.updateActive()
            return con
        }
    }

    suspend fun <T> borrow(func: suspend PooledAsyncConnectionImpl.() -> T): T =
        getConnectionAnyWay().use {
            func(it)
        }

    internal suspend fun free(sql: String) {
        connections.forEach {
            it.closePreparedStatement(sql)
        }
    }

    internal suspend fun free(connection: PooledAsyncConnectionImpl) {
        cleanUp()
        val w = getOneWater()
        if (w == null) {
            idleConnection += connection
        } else {
            w.resume(connection)
        }
    }

    override suspend fun asyncClose() {
        waiters.forEach {
            runCatching { it.resumeWithException(StreamClosedException()) }
        }
        waiters.clear()
        connections.forEach {
            runCatching { it.asyncClose() }
        }
        connections.clear()
        idleConnection.clear()
    }
}