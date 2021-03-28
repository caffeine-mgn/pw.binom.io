package pw.binom.db.pool

import pw.binom.date.Date
import pw.binom.db.AsyncConnection
import pw.binom.db.AsyncPreparedStatement
import pw.binom.db.AsyncStatement
import pw.binom.io.use
import pw.binom.neverFreeze
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PooledAsyncConnection(val pool: AsyncConnectionPool, val connection: AsyncConnection) :
    AsyncConnection by connection {

    private val qq = HashMap<String, AsyncPreparedStatement>()
    private val forRemove = HashMap<String, AsyncPreparedStatement>()


    internal fun getOrCreate(sql: String): AsyncPreparedStatement {
        val p = forRemove.remove(sql)
        if (p != null) {
            qq[sql] = p
            return p
        }
        return qq.getOrPut(sql) { connection.prepareStatement(sql) }
    }

    internal suspend fun free(sql: String) {
        val ps = qq.remove(sql) ?: return
        if (connection.isReadyForQuery()) {
            ps.asyncClose()
        } else {
            forRemove[sql] = ps
        }
    }

    var lastCheckTime = Date.now
    var lastActive = 0L
        private set

    fun updateActive() {
        lastActive = Date.now
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
        if (Date.now - lastCheckTime > pool.pingTime.inMilliseconds.toLong()) {
            if (isReadyForQuery()) {
                try {
                    connection.createStatement().use { it.executeQuery("select 1").asyncClose() }
                } catch (e: Throwable) {
                    invalid = true
                    return false
                }
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
        clean()
        prepareStatements.toTypedArray().forEach {
            it.asyncClose()
        }
        prepareStatements.clear()
        pool.free(this)
    }
}