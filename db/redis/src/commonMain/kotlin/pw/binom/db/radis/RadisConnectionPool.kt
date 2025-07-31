package pw.binom.db.radis

import pw.binom.collections.LinkedList
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.date.DateTime
import pw.binom.io.ByteBuffer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class RadisConnectionPool(
  val idle: Duration = 5.minutes,
  val maxConnection: Int = 10,
  val factory: suspend () -> RadisConnection,
) : RadisConnection {
  private val connections = LinkedList<Item>()
  private val connectionLock = SpinLock()

  private class Item(
    val connection: RadisConnection,
  ) {
    val date = DateTime.now
  }

  override val readyForRequest: Boolean
    get() = true

  private suspend fun borrow(): RadisConnection {
    connectionLock.synchronize {
      val it = connections.iterator()
      while (it.hasNext()) {
        val e = it.next()
        if (DateTime.now - e.date > idle) {
          e.connection.asyncClose()
          it.remove()
        } else {
          val con = e.connection
          it.remove()
          return con
        }
      }
    }
    return factory()
  }

  private suspend fun free(connection: RadisConnection) {
    connectionLock.synchronize {
      if (connections.size >= maxConnection) {
        connection.asyncClose()
      } else {
        connections.addLast(Item(connection))
      }
    }
  }

  private suspend inline fun <T> borrow(func: suspend (RadisConnection) -> T): T {
    val con = borrow()
    return try {
      func(con)
    } finally {
      free(con)
    }
  }

  override suspend fun setString(
    key: String,
    value: String,
    ttl: Duration?,
    updateMode: UpdateMode,
  ) = borrow {
    it.setString(
      key = key,
      value = value,
      ttl = ttl,
      updateMode = updateMode,
    )
  }

  override suspend fun delete(vararg key: String): Long =
    borrow {
      it.delete(*key)
    }

  override suspend fun delete(key: String): Boolean =
    borrow {
      it.delete(key)
    }

  override suspend fun lset(key: String, value: String, index: Int) =
    borrow {
      it.lset(
        key = key,
        value = value,
        index = index,
      )
    }

  override suspend fun inc(key: String): Long? =
    borrow {
      it.inc(key)
    }

  override suspend fun inc(key: String, value: Int): Long? =
    borrow {
      it.inc(
        key = key,
        value = value,
      )
    }

  override suspend fun inc(key: String, value: Float) =
    borrow {
      it.inc(
        key = key,
        value = value,
      )
    }

  override suspend fun renameKey(oldName: String, newName: String) =
    borrow {
      it.renameKey(
        oldName = oldName,
        newName = newName,
      )
    }

  override suspend fun getList(
    key: String,
    start: Int,
    end: Int,
  ): List<String>? =
    borrow {
      it.getList(
        key = key,
        start = start,
        end = end,
      )
    }

  override suspend fun getListSize(key: String): Long? =
    borrow {
      it.getListSize(
        key = key,
      )
    }

  override suspend fun setStringAsBytes(
    key: String,
    data: ByteArray,
    ttl: Duration?,
    updateMode: UpdateMode,
  ) =
    borrow {
      it.setStringAsBytes(
        key = key,
        data = data,
        ttl = ttl,
        updateMode = updateMode,
      )
    }

  override suspend fun setStringAsBytes(
    key: String,
    data: ByteBuffer,
    ttl: Duration?,
    updateMode: UpdateMode,
  ) = borrow {
    it.setStringAsBytes(
      key = key,
      data = data,
      ttl = ttl,
      updateMode = updateMode,
    )
  }

  override suspend fun getString(key: String): String? =
    borrow {
      it.getString(
        key = key,
      )
    }

  override suspend fun getStringAsByteArray(key: String): ByteArray? =
    borrow {
      it.getStringAsByteArray(
        key = key,
      )
    }

  override suspend fun insertLast(key: String, value: String): Long? =
    borrow {
      it.insertLast(
        key = key,
        value = value,
      )
    }

  override suspend fun insertFirst(key: String, value: String): Long? =
    borrow {
      it.insertLast(
        key = key,
        value = value,
      )
    }

  override suspend fun asyncClose() {

  }
}
