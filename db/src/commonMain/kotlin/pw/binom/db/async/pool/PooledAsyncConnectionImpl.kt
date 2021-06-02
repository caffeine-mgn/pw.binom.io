package pw.binom.db.async.pool

import pw.binom.date.Date
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.AsyncStatement
import pw.binom.io.use
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PooledAsyncConnectionImpl(val pool: AsyncConnectionPoolImpl, val connection: AsyncConnection) :
    PooledAsyncConnection, AsyncConnection by connection {

    private val createdPreparedStatement = HashMap<String, AsyncPreparedStatement>()
    private val forRemove = HashMap<String, AsyncPreparedStatement>()


    override suspend fun usePreparedStatement(sql: String): AsyncPreparedStatement {
        val p = forRemove.remove(sql)
        if (p != null) {
            createdPreparedStatement[sql] = p
            return p
        }
        return createdPreparedStatement.getOrPut(sql) { connection.prepareStatement(sql) }
    }

    override suspend fun closePreparedStatement(sql: String) {
        val ps = createdPreparedStatement.remove(sql) ?: return
        if (connection.isReadyForQuery()) {
            ps.asyncClose()
        } else {
            forRemove[sql] = ps
        }
    }

    private var lastCheckTime = Date.nowTime
    var lastActive = 0L
        private set

    fun updateActive() {
        lastActive = Date.nowTime
    }

    private var invalid = false

    private suspend fun clean() {
        if (!connection.isReadyForQuery()) {
            return
        }
        if (forRemove.isNotEmpty() && connection.isReadyForQuery()) {
            forRemove.forEach {
                it.value.asyncClose()
            }
            forRemove.clear()
        }
    }

    suspend fun checkValid(): Boolean {
        if (invalid) {
            return false
        }
        if (!connection.isConnected) {
            return false
        }
        clean()
        if (Date.nowTime - lastCheckTime > pool.pingTime.inMilliseconds.toLong()) {
            if (isReadyForQuery()) {
                try {
                    connection.createStatement().use { it.executeQuery("select 1").asyncClose() }
                } catch (e: Throwable) {
                    invalid = true
                    return false
                }
            }
            lastCheckTime = Date.nowTime
        }
        return true
    }

    override suspend fun createStatement(): AsyncStatement = connection.createStatement()
    internal val prepareStatements = HashSet<PooledAsyncPreparedStatement>()

    override suspend fun prepareStatement(query: String): AsyncPreparedStatement {
        val pst = connection.prepareStatement(query)
        val pooledPst = PooledAsyncPreparedStatement(this, pst)
        prepareStatements += pooledPst
        return pooledPst
    }

    override suspend fun asyncClose() {
        clean()
        prepareStatements.toTypedArray().forEach {
            it.asyncClose()
        }
        prepareStatements.clear()
        pool.free(this)
    }
}