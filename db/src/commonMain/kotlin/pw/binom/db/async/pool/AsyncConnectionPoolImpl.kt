package pw.binom.db.async.pool

import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.date.Date
import pw.binom.db.async.AsyncConnection
import pw.binom.io.StreamClosedException
import pw.binom.io.use
import pw.binom.neverFreeze
import kotlin.coroutines.*
import kotlin.time.*
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalTime::class)
class AsyncConnectionPoolImpl constructor(
    val maxConnections: Int,
    val pingTime: Duration = 1.0.minutes,
    val idleTime: Duration = 5.0.minutes,
    val waitFreeConnection: Boolean = true,
    val factory: suspend () -> AsyncConnection,
) : AsyncConnectionPool {

    init {
        require(maxConnections >= 1) { "maxConnections should be grate than 0" }
    }

    private val connections = HashSet<PooledAsyncConnectionImpl>(maxConnections)
    private val idleConnection = ArrayList<PooledAsyncConnectionImpl>(maxConnections)

    private val waiters = ArrayList<Continuation<PooledAsyncConnectionImpl>>()
    private val idleConnectionLock = SpinLock()
    private val connectionsLock = SpinLock()

    init {
        neverFreeze()
    }

    override val idleConnectionCount
        get() = idleConnectionLock.synchronize { idleConnection.size }

    override val connectionCount
        get() = connectionsLock.synchronize { connections.size }

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

    private var lastCleanup = TimeSource.Monotonic.markNow()

    suspend fun cleanUp(): Int {
        if (lastCleanup.elapsedNow() < idleTime) {
            return 0
        }
        lastCleanup = TimeSource.Monotonic.markNow()
        var count = 0
        var hasRemovedAny = false
        if (cleaning) {
            return 0
        }
        try {
            cleaning = true
            idleConnectionLock.synchronize {
                val it = idleConnection.iterator()
                while (it.hasNext()) {
                    val e = it.next()
                    if (Date.nowTime - e.lastActive > idleTime.toLong(DurationUnit.MILLISECONDS)) {
                        it.remove()
                        connectionsLock.synchronize {
                            connections -= e
                        }
                        forRemove += e
                        count++
                        continue
                    }
                }
            }
            if (forRemove.isNotEmpty()) {
                hasRemovedAny=true
                forRemove.forEach {
                    runCatching { it.fullClose() }
                }
                forRemove.clear()
            }
        } finally {
            cleaning = false
        }
        val idleSize = idleConnectionLock.synchronize { idleConnection.size }
        val activeSize = connectionsLock.synchronize { connections.size }
        return count
    }

    private suspend fun getConnectionAnyWay(): PooledAsyncConnectionImpl {
        while (true) {
            val connection = idleConnectionLock.synchronize { idleConnection.removeLastOrNull() }
            if (connection != null) {
                if (!connection.checkValid()) {
                    connectionsLock.synchronize {
                        connections -= connection
                    }
                    forRemove += connection
                    continue
                }
                return connection
            }
            val needCreateNew = connectionsLock.synchronize { connections.size < maxConnections }
            if (needCreateNew) {
                val con = PooledAsyncConnectionImpl(this, factory())
                connectionsLock.synchronize { connections.add(con) }
                return con
            }
            if (!waitFreeConnection) {
                throw IllegalStateException("No free connections")
            }

            val con = suspendCoroutine<PooledAsyncConnectionImpl> { waiters += it }
            if (!con.checkValid()) {
                connectionsLock.synchronize {
                    connections -= con
                }
                forRemove += con
                continue
            }
            con.updateActive()
            return con
        }
    }

    override suspend fun <T> borrow(func: suspend PooledAsyncConnection.() -> T): T =
        getConnectionAnyWay().use {
            func(it)
        }

    internal suspend fun free(sql: String) {
        val connections = connectionsLock.synchronize {
            connections.toTypedArray()
        }
        connections.forEach {
            it.closePreparedStatement(sql)
        }
    }

    internal suspend fun free(connection: PooledAsyncConnectionImpl) {
        cleanUp()
        val w = getOneWater()
        if (w == null) {
            idleConnectionLock.synchronize {
                idleConnection += connection
            }
        } else {
            w.resume(connection)
        }
    }

    override suspend fun asyncClose() {
        waiters.forEach {
            runCatching { it.resumeWithException(StreamClosedException()) }
        }
        waiters.clear()
        ArrayList(connections).forEach {
            runCatching { it.asyncClose() }
        }
        connections.clear()
        idleConnectionLock.synchronize {
            idleConnection.forEach {
                it.fullClose()
            }
            idleConnection.clear()
        }
    }
}