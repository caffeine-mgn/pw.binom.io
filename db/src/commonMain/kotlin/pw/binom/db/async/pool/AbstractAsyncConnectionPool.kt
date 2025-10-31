package pw.binom.db.async.pool

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import pw.binom.collection.WeakReferenceMap
import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableSet
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.date.DateTime
import pw.binom.db.ConnectionMaxLiveTimeException
import pw.binom.db.async.AsyncConnection
import pw.binom.io.StreamClosedException
import pw.binom.io.useAsync
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

abstract class AbstractAsyncConnectionPool : AsyncConnectionPool {
  abstract val maxConnections: Int
  open val pingTime: Duration = 1.0.minutes
  open val idleTime: Duration = 5.0.minutes
  open val maxLifetime: Duration = 18.0.seconds
  open val waitFreeConnection: Boolean = true
  protected abstract suspend fun createConnection(): AsyncConnection


  private val connections = defaultMutableSet<PooledAsyncConnectionImpl>()
  private val idleConnection by lazy { ArrayList<PooledAsyncConnectionImpl>(maxConnections) }

  private val waiters = ArrayList<Continuation<PooledAsyncConnectionImpl>>()
  private val waitersLock = SpinLock()
  private val idleConnectionLock = SpinLock()
  private val connectionsLock = SpinLock()
  private val avalibles = WeakReferenceMap<PooledAsyncConnection, Boolean>()

  override val idleConnectionCount
    get() = idleConnectionLock.synchronize { idleConnection.size }

  override val connectionCount
    get() = connectionsLock.synchronize { connections.size }

  private var cleaning = false

  private fun getOneWater(): Continuation<PooledAsyncConnectionImpl>? =
    waitersLock.synchronize { waiters.removeLastOrNull() }

  fun prepareStatement(sql: String) {
    PooledAsyncPreparedStatement2(this, sql)
  }

  private var lastCleanup = TimeSource.Monotonic.markNow()

  override suspend fun cleanUp(): Int {
    var count = 0
    val forRemove = defaultMutableSet<PooledAsyncConnectionImpl>()
    try {
      cleaning = true
      idleConnectionLock.synchronize {
        val it = idleConnection.iterator()
        while (it.hasNext()) {
          val e = it.next()
          val connectionIdleTime = (DateTime.nowTime - e.lastActive).milliseconds
          if (connectionIdleTime > idleTime) {
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
        forRemove.forEach {
          avalibles[it] = true
          runCatching { it.fullClose() }
        }
        forRemove.clear()
      }
    } finally {
      cleaning = false
    }
    return count
  }

  suspend fun checkCleanUp(): Int {
    if (lastCleanup.elapsedNow() < idleTime) {
      return 0
    }
    lastCleanup = TimeSource.Monotonic.markNow()
    if (cleaning) {
      return 0
    }
    return cleanUp()
  }

  private suspend fun getConnectionAnyWay(): PooledAsyncConnectionImpl {
    while (true) {
      val connection = idleConnectionLock.synchronize { idleConnection.removeLastOrNull() }
      if (connection != null) {
        if (!connection.checkValid()) {
          connectionsLock.synchronize {
            connections -= connection
          }
          avalibles[connection] = true
          kotlin.runCatching { connection.fullClose() }
          continue
        }
        connection.updateActive()
        return connection
      }
      val needCreateNew = connectionsLock.synchronize { connections.size < maxConnections }
      if (needCreateNew) {
        val con = PooledAsyncConnectionImpl(this, createConnection())
        connectionsLock.synchronize { connections.add(con) }
        return con
      }
      if (!waitFreeConnection) {
        throw IllegalStateException("No free connections")
      }

      val con = suspendCoroutine<PooledAsyncConnectionImpl> {
        waitersLock.synchronize {
          waiters += it
        }
      }
      if (!con.checkValid()) {
        connectionsLock.synchronize {
          connections -= con
        }
        runCatching { con.fullClose() }
        continue
      }
      con.updateActive()
      return con
    }
  }

  override suspend fun getConnection(): PooledAsyncConnection = getConnectionAnyWay()

  override suspend fun <T> borrow(func: suspend PooledAsyncConnection.() -> T): T {
    val maxLifetime = maxLifetime
    return getConnection().useAsync { connection ->
      if (maxLifetime.isInfinite()) {
        func(connection)
      } else {
        try {
          withTimeout(maxLifetime) {
            func(connection)
          }
        } catch (e: TimeoutCancellationException) {
          throw ConnectionMaxLiveTimeException()
        }
      }
    }
  }

  internal suspend fun free(sql: String) {
    val connections =
      connectionsLock.synchronize {
        connections.toTypedArray()
      }
    connections.forEach {
      it.closePreparedStatement(sql)
    }
  }

  internal suspend fun free(connection: PooledAsyncConnectionImpl) {
    checkCleanUp()
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
    val watersLocal = waitersLock.synchronize {
      val newWaters = ArrayList(waiters)
      waiters.clear()
      newWaters
    }
    watersLocal.forEach {
      it.resumeWithException(StreamClosedException())
    }
    defaultMutableList(connections).forEach {
      it.asyncCloseAnyway()
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
