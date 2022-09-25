package pw.binom.db.async.pool

import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.date.DateTime
import pw.binom.db.async.AsyncConnection
import pw.binom.io.StreamClosedException
import pw.binom.io.use
import pw.binom.neverFreeze
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class AsyncConnectionPoolImpl constructor(
    val maxConnections: Int,
    val pingTime: Duration = 1.0.minutes,
    val idleTime: Duration = 5.0.minutes,
    val waitFreeConnection: Boolean = true,
    val factory: suspend () -> AsyncConnection,
) : AsyncConnectionPool {
    private fun log(txt: String) {
        println("AsyncConnectionPoolImpl: $txt")
    }

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
                    if (DateTime.nowTime - e.lastActive > idleTime.toLong(DurationUnit.MILLISECONDS)) {
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
                hasRemovedAny = true
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
                    log("found invalid connection.... connection.checkValid()=${connection.checkValid()}")
                    forRemove += connection
                    continue
                }
                log("got connection from pool")
                connection.updateActive()
                return connection
            }
            val needCreateNew = connectionsLock.synchronize { connections.size < maxConnections }
            if (needCreateNew) {
                val con = PooledAsyncConnectionImpl(this, factory())
                connectionsLock.synchronize { connections.add(con) }
                log("return NEW connection")
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
                log("Connection got, but invalid :(")
                continue
            }
            con.updateActive()
            log("return connection....")
            return con
        }
    }

    override suspend fun <T> borrow(func: suspend PooledAsyncConnection.() -> T): T {
        val out = getConnectionAnyWay().use {
            func(it)
        }
        return out
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
