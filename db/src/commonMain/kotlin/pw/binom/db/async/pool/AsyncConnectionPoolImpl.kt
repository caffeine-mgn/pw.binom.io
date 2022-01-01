package pw.binom.db.async.pool

import com.ionspin.kotlin.bignum.integer.concurrent.concurrentMultiply
import pw.binom.concurrency.Lock
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.date.Date
import pw.binom.db.async.AsyncConnection
import pw.binom.io.StreamClosedException
import pw.binom.io.use
import pw.binom.logger.Logger
import pw.binom.logger.debug
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
    private val logger = Logger.getLogger("AsyncConnectionPoolImpl")

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
                logger.debug("Total closed connection ${forRemove.size}")
                forRemove.forEach {
                    runCatching { it.fullClose() }
                }
                forRemove.clear()
            }
        } finally {
            cleaning = false
        }
        if (count > 0) {
            logger.debug("Not used closed connection $count")
        }
        val idleSize = idleConnectionLock.synchronize { idleConnection.size }
        val activeSize = connectionsLock.synchronize { connections.size }
        if (idleSize>0 || activeSize>0 || hasRemovedAny) {
            logger.debug("Status after cleanup: Active connection size: $activeSize, Idle connection size: $idleSize")
        }
        return count
    }

    private suspend fun getConnectionAnyWay(): PooledAsyncConnectionImpl {
        while (true) {
            val connection = idleConnectionLock.synchronize { idleConnection.removeLastOrNull() }
            if (connection != null) {
                if (!connection.checkValid()) {
                    logger.debug("New connection is invalid. Try again")
                    connectionsLock.synchronize {
                        connections -= connection
                    }
                    forRemove += connection
                    continue
                } else {
                    val idleSize = idleConnectionLock.synchronize { idleConnection.size }
                    logger.debug("Getting idle connection. Idle connections count: $idleSize")
                }
                return connection
            } else {
                logger.debug("No idle connection")
            }
            logger.debug("Try to spawn new connection")
            connectionsLock.lock()
            if (connections.size < maxConnections) {
                logger.debug("Allocation new connection...")
                connectionsLock.unlock()
                val con = PooledAsyncConnectionImpl(this, factory())
                connectionsLock.lock()
                connections.add(con)
                connectionsLock.unlock()
                logger.debug("New connection Allocated! Connection count: $connections")
                return con
            } else {
                logger.debug("No free connections")
            }
            if (!waitFreeConnection) {
                throw IllegalStateException("No free connections")
            }
            logger.debug("Wait until any connect free")

            val con = suspendCoroutine<PooledAsyncConnectionImpl> { waiters += it }
            logger.debug("Found idle connection")
            if (!con.checkValid()) {
                logger.debug("Founded connection is invalid")
                connectionsLock.synchronize {
                    connections -= con
                }
                forRemove += con
                continue
            }
            logger.debug("Founded connection is valid. Returning it")
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
                logger.debug("Connection free. Push to idle connections. Idle connection: ${idleConnection.size}")
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