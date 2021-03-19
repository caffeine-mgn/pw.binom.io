package pw.binom.db

import pw.binom.io.AsyncCloseable
import pw.binom.io.use
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.minutes

@OptIn(ExperimentalTime::class)
class AsyncConnectionPool constructor(
    val max: Int,
    val pingTime: Duration = 1.0.minutes,
    val idleTime: Duration = 10.0.minutes,
    val waitFreeConnection: Boolean = true,
    val gcInterval: Duration = 1.0.minutes,
    val factory: suspend () -> AsyncConnection,
) : AsyncCloseable {
    private val connections = ArrayList<AsyncConnectionPooled>(max)

    private val waiters = ArrayList<Continuation<AsyncConnection>>()
    private var lastGc = TimeSource.Monotonic.markNow()

    /**
     * Cleanup idle connections
     */
    suspend fun forceGc() {
        lastGc = TimeSource.Monotonic.markNow()
        val it = connections.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (!e.free) {
                continue
            }
            if (!e.isConnected || e.ioException) {
                it.remove()
                continue
            }
            if (e.lastAction.elapsedNow() > idleTime) {
                it.remove()
                e.stream.asyncClose()
            }
        }
    }

    private suspend fun checkGc() {
        if (lastGc.elapsedNow() > gcInterval) {
            forceGc()
        }
    }

    private suspend fun findValidConnection(): AsyncConnectionPooled? {
        val it = connections.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (!e.free) {
                continue
            }
            if (!e.isConnected || e.ioException) {
                it.remove()
                continue
            }
            if (e.lastAction.elapsedNow() > pingTime) {
                if (e.ping()) {
                    return e
                } else {
                    it.remove()
                }
                continue
            }
            return e
        }
        return null
    }

    suspend fun <T> connect(func: suspend (AsyncConnection) -> T): T =
        getConnection().use {
            func(it)
        }

    private suspend fun getConnection(): AsyncConnection {

        var connection = findValidConnection()
        if (connection == null && connections.size < max) {
            connection = AsyncConnectionPooled(factory())
            connections += connection
            connection.reset()
        }
        if (connection == null && !waitFreeConnection) {
            throw IllegalStateException("No free Database Connection")
        }
        checkGc()
        if (connection != null) {
            connection.reset()
            return connection
        }
        return suspendCoroutine { waiters += it }
    }

    private suspend fun free(connection: AsyncConnectionPooled) {
        if (waitFreeConnection) {
            val waiter = waiters.removeLastOrNull() ?: return
            connection.reset()
            checkGc()
            waiter.resume(connection)
        } else {
            checkGc()
        }
    }

    private inner class AsyncConnectionPooled(val stream: AsyncConnection) : AsyncConnection {

        private var closed = false
        var lastAction = TimeSource.Monotonic.markNow()
            private set

        var ioException = false
            private set

        val free
            get() = closed

        fun reset() {
            closed = false
        }

        suspend fun ping(): Boolean {
            if (ioException) {
                return false
            }
            return try {
                stream.createStatement().use {
                    val r = it.executeQuery("select 1")
                    while (true) {
                        if (!r.next()) {
                            break
                        }
                    }
                }
                lastAction = TimeSource.Monotonic.markNow()
                true
            } catch (e: Throwable) {
                ioException = true
                runCatching { stream.asyncClose() }
                false
            }
        }

        override fun createStatement(): AsyncStatement = stream.createStatement()

        override fun prepareStatement(query: String): AsyncPreparedStatement =
            stream.prepareStatement(query)

        override suspend fun commit() {
            stream.commit()
        }

        override suspend fun rollback() {
            stream.rollback()
        }

        override val isConnected: Boolean
            get() = stream.isConnected

        override val type: String
            get() = stream.type

        private fun checkClosed() {
            if (closed) {
                throw IllegalStateException("Connection already closed")
            }
        }

        override suspend fun asyncClose() {
            checkClosed()
            closed = true
            free(this)
        }
    }

    override suspend fun asyncClose() {
        connections.forEach {
            it.asyncClose()
        }
    }
}