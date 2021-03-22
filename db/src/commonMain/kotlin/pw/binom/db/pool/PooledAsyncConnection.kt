package pw.binom.db.pool

import pw.binom.date.Date
import pw.binom.db.AsyncConnection
import pw.binom.db.AsyncPreparedStatement
import pw.binom.db.AsyncStatement
import pw.binom.io.use
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PooledAsyncConnection(val pool: AsyncConnectionPool, val connection: AsyncConnection) :
    AsyncConnection by connection {

    var lastCheckTime = Date.now
    var lastActive = 0L
        private set

    fun updateActive() {
        lastActive = Date.now
    }

    private var invalid = false

    suspend fun checkValid(): Boolean {
        if (invalid) {
            return false
        }
        if (!connection.isConnected) {
            return false
        }
        if (Date.now - lastCheckTime > pool.pingTime.inMilliseconds.toLong()) {
            try {
                connection.createStatement().use { it.executeQuery("select 1").asyncClose() }
            } catch (e: Throwable) {
                invalid = true
                return false
            }
            lastCheckTime = Date.now
        }
        return true
    }

    override fun createStatement(): AsyncStatement = connection.createStatement()
    internal val prepareStatements = HashSet<PooledAsyncPreparedStatement>()

    override fun prepareStatement(query: String): AsyncPreparedStatement {
        val pst = connection.prepareStatement(query)
        val pooledPst = PooledAsyncPreparedStatement(this, pst)
        prepareStatements += pooledPst
        return pooledPst
    }

    override suspend fun asyncClose() {
        prepareStatements.toTypedArray().forEach {
            it.asyncClose()
        }
        prepareStatements.clear()
        pool.free(this)
    }
}