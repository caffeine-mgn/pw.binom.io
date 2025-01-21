package pw.binom.db.async

import pw.binom.db.async.pool.AsyncConnectionPool
import pw.binom.db.async.pool.PooledAsyncConnection

abstract class AbstractAsyncConnectionPoolWrapper : AsyncConnectionPool {
  protected abstract fun initConnection(): AsyncConnectionPool

  private val pool by lazy {
    initConnection()
  }

  override val idleConnectionCount: Int
    get() = pool.idleConnectionCount
  override val connectionCount: Int
    get() = pool.connectionCount

  override suspend fun getConnection(): PooledAsyncConnection =
    pool.getConnection()

  override suspend fun <T> borrow(func: suspend PooledAsyncConnection.() -> T): T =
    pool.borrow(func)

  override suspend fun cleanUp(): Int =
    pool.cleanUp()

  override suspend fun asyncClose() = pool.asyncClose()
}
