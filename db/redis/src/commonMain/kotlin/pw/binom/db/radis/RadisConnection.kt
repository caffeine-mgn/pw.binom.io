package pw.binom.db.radis

import kotlinx.coroutines.Dispatchers
import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteBuffer
import pw.binom.io.socket.SocketAddress
import pw.binom.network.Network
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect
import kotlin.time.Duration

interface RadisConnection : AsyncCloseable {
  enum class ValueType {
    STRING,
    LIST,
    SET,
    ZSET,
    HASH,
    STREAM,
  }

  val readyForRequest: Boolean

  companion object {
    suspend fun connect(
      address: SocketAddress,
      manager: NetworkManager = Dispatchers.Network,
      login: String? = null,
      password: String? = null,
    ): RadisConnectionImpl {
      val con =
        RadisConnectionImpl(
          connection = manager.tcpConnect(address.resolve()),
          bufferSize = 30,
        )
      try {
        con.start()
        return con
      } catch (e: Throwable) {
        con.asyncClose()
        throw e
      }
    }
  }

  suspend fun setString(
    key: String,
    value: String,
    ttl: Duration? = null,
    updateMode: UpdateMode = UpdateMode.ANYWAY,
  )

  suspend fun delete(vararg key: String): Long

  suspend fun delete(key: String): Boolean

  suspend fun lset(
    key: String,
    value: String,
    index: Int,
  )

  suspend fun inc(key: String): Long?
  suspend fun inc(
    key: String,
    value: Int,
  ): Long?

  suspend fun inc(
    key: String,
    value: Float,
  ): Double?

  suspend fun renameKey(
    oldName: String,
    newName: String,
  )

  suspend fun getList(
    key: String,
    start: Int = 0,
    end: Int = -1,
  ): List<String>?

  suspend fun getListSize(key: String): Long?

  suspend fun setStringAsBytes(
    key: String,
    data: ByteArray,
    ttl: Duration?,
    updateMode: UpdateMode = UpdateMode.ANYWAY,
  )

  suspend fun setStringAsBytes(
    key: String,
    data: ByteBuffer,
    ttl: Duration?,
    updateMode: UpdateMode = UpdateMode.ANYWAY,
  )

  suspend fun getString(key: String): String?

  suspend fun getStringAsByteArray(key: String): ByteArray?

  suspend fun insertLast(
    key: String,
    value: String,
  ): Long?

  suspend fun insertFirst(
    key: String,
    value: String,
  ): Long?
}
