package pw.binom.db.async.pool

import pw.binom.date.DateTime
import pw.binom.db.async.AsyncConnection
import pw.binom.db.async.AsyncPreparedStatement
import pw.binom.db.async.AsyncStatement
import pw.binom.io.use
import kotlin.time.DurationUnit
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

    private var lastCheckTime = DateTime.nowTime
    var lastActive = 0L
        private set

    fun updateActive() {
        lastActive = DateTime.nowTime
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
        if (DateTime.nowTime - lastCheckTime > pool.pingTime.toDouble(DurationUnit.MILLISECONDS).toLong()) {
            if (isReadyForQuery()) {
                try {
                    connection.createStatement().use { it.executeQuery("select 1").asyncClose() }
                } catch (e: Throwable) {
                    invalid = true
                    return false
                }
            }
            lastCheckTime = DateTime.nowTime
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

    private suspend fun cleanUp() {
        clean()
        val statements = ArrayList(prepareStatements)
        prepareStatements.clear()
        statements.forEach {
            it.asyncClose()
        }
    }

    internal suspend fun fullClose() {
        cleanUp()
        if (connection.isConnected) {
            connection.asyncClose()
        }
    }

    override suspend fun asyncClose() {
        cleanUp()
        pool.free(this)
    }
}
