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
    private val connections = HashSet<PooledAsyncConnection>(maxConnections)
    private val idleConnection = ArrayList<PooledAsyncConnection>(maxConnections)

    private val waiters = ArrayList<Continuation<PooledAsyncConnection>>()

    val idleConnectionCount
        get() = idleConnection.size

    val connectionCount
        get() = connections.size

    /**
     * Set for connection for delete
     */
    private
    val forRemove = HashSet<PooledAsyncConnection>()
    private var cleaning = false

    private fun getOneWater(): Continuation<PooledAsyncConnection>? =
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

    private suspend fun getConnectionAnyWay(): PooledAsyncConnection {
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
                val con = PooledAsyncConnection(this, factory())
                connections.add(con)
                return con
            }
            if (!waitFreeConnection) {
                throw IllegalStateException("No free connections")
            }

            val con = suspendCoroutine<PooledAsyncConnection> { waiters += it }
            if (!con.checkValid()) {
                connections -= con
                forRemove += con
                continue
            }
            con.updateActive()
            return con
        }
    }

    suspend fun borrow() = getConnectionAnyWay()
    suspend fun <T> borrow(func: suspend (PooledAsyncConnection) -> T): T =
        borrow().use {
            func(it)
        }

//    suspend fun update(sql: String, arguments: List<Any?>): Long =
//        getConnection { con ->
//            try {
//                if (arguments.isEmpty()) {
//                    con.createStatement().use {
//                        it.executeUpdate(sql)
//                    }
//                } else {
//                    con.prepareStatement(sql).use { st ->
//                        arguments.forEachIndexed { index, arg ->
//                            st.setValue(index, arg)
//                        }
//                        st.executeUpdate()
//                    }
//                }
//            } catch (e: SQLException) {
//                throw SQLException("Can't execute \"$sql\"", e)
//            }
//        }

//    suspend fun update(sql: String, vararg arguments: Any?): Long =
//        getConnection { con ->
//            try {
//                if (arguments.isEmpty()) {
//                    con.createStatement().use {
//                        it.executeUpdate(sql)
//                    }
//                } else {
//                    con.prepareStatement(sql).use { st ->
//                        arguments.forEachIndexed { index, arg ->
//                            st.setValue(index, arg)
//                        }
//                        st.executeUpdate()
//                    }
//                }
//            } catch (e: SQLException) {
//                throw SQLException("Can't execute \"$sql\"", e)
//            }
//        }

//    suspend fun updateBatch(sql: String, values: List<List<Any?>>): Long {
//        var count = 0L
//        getConnection { con ->
//            try {
//                con.prepareStatement(sql).use { st ->
//                    values.forEach { arguments ->
//                        arguments.forEachIndexed { index, arg ->
//                            st.setValue(index, arg)
//                        }
//                        count += st.executeUpdate()
//                    }
//                }
//            } catch (e: SQLException) {
//                throw SQLException("Can't execute \"$sql\"", e)
//            }
//        }
//        return count
//    }

//    suspend fun <T> selectAll(sql: String, vararg arguments: Any?, mapper: suspend (ResultSet) -> T) =
//        getConnection { con ->
//            try {
//                con.prepareStatement(sql).use { st ->
//                    arguments.forEachIndexed { index, arg ->
//                        st.setValue(index, arg)
//                    }
//                    st.executeQuery().list {
//                        mapper(it)
//                    }
//                }
//            } catch (e: SQLException) {
//                throw SQLException("Can't execute \"$sql\"", e)
//            }
//        }

//    suspend fun <T> selectFirst(sql: String, vararg arguments: Any?, mapper: suspend (ResultSet) -> T) {
//        getConnection { con ->
//            try {
//                con.prepareStatement(sql).use { st ->
//                    arguments.forEachIndexed { index, arg ->
//                        st.setValue(index, arg)
//                    }
//                    st.executeQuery().use {
//                        if (it.next()) {
//                            mapper(it)
//                        } else {
//                            it.asyncClose()
//                        }
//                    }
//                }
//            } catch (e: SQLException) {
//                throw SQLException("Can't execute \"$sql\"", e)
//            }
//        }
//    }

//    private suspend fun <T> getConnection(func: suspend (AsyncConnection) -> T): T {
//        val con = getConnectionAnyWay()
//        return try {
//            func(con.connection)
//        } finally {
//            free(con)
//        }
//    }

    internal suspend fun free(sql: String) {
        connections.forEach {
            it.free(sql)
        }
    }

    internal suspend fun free(connection: PooledAsyncConnection) {
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