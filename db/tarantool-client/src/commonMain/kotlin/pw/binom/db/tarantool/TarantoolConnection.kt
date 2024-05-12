package pw.binom.db.tarantool

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.binom.asUTF8String
import pw.binom.db.tarantool.protocol.Code
import pw.binom.db.tarantool.protocol.InternalProtocolUtils
import pw.binom.db.tarantool.protocol.QueryIterator
import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteBuffer
import pw.binom.io.socket.SocketAddress
import pw.binom.io.use
import pw.binom.network.Network
import pw.binom.network.NetworkManager
import pw.binom.network.tcpConnect

interface TarantoolConnection : AsyncCloseable {
  companion object {
    suspend fun connect(
        manager: NetworkManager = Dispatchers.Network,
        address: SocketAddress,
        userName: String?,
        password: String?,
    ): TarantoolConnectionImpl {
      val con = manager.tcpConnect(address.resolve())
      ByteBuffer(64).use { buf ->
        var connection: TarantoolConnectionImpl? = null
        try {
          con.readFully(buf)
          buf.flip()
          val version = buf.asUTF8String().trim().substring(10)
          buf.clear()
          con.readFully(buf)
          buf.flip()
          val salt = buf.asUTF8String().trim()
          connection =
            TarantoolConnectionImpl(
//                        networkThread = ThreadRef(),
              networkDispatcher = manager,
              connection = con,
              serverVersion = version,
            )
          connection.mainLoopJob = GlobalScope.launch(manager) { connection.startMainLoop() }
          if ((userName == null && password != null) || userName != null && password == null) {
            throw IllegalArgumentException("Login or password is invalid")
          }
          if (userName != null && password != null) {
            connection.sendReceive(
              code = Code.AUTH,
              schemaId = null,
              body = InternalProtocolUtils.buildAuthPacketData(userName, password, salt),
            ).assertException()
          }

          return connection
        } catch (e: Throwable) {
          connection?.asyncClose()
          throw e
        }
      }
    }
  }

  suspend fun getMeta(): List<TarantoolSpaceMeta>

  suspend fun ping()

  suspend fun insert(
    space: Int,
    values: List<Any?>,
  )

  suspend fun insert(
    space: String,
    values: List<Any?>,
  )

  suspend fun delete(
    space: Int,
    keys: List<Any?>,
  ): Row?

  suspend fun upsert(
    space: Int,
    indexValues: List<Any?>,
    values: List<FieldUpdate>,
  )

  suspend fun upsert(
    space: String,
    indexValues: List<Any?>,
    values: List<FieldUpdate>,
  )

  suspend fun update(
    space: Int,
    key: List<Any?>,
    values: List<FieldUpdate>,
  ): Row?

  suspend fun update(
    space: String,
    key: List<Any?>,
    values: List<FieldUpdate>,
  ): Row?

  suspend fun replace(
    space: Int,
    values: List<Any?>,
  )

  suspend fun replace(
    space: String,
    values: List<Any?>,
  )

  suspend fun delete(
    space: String,
    keys: List<Any?>,
  ): Row?

  suspend fun select(
    space: Int,
    index: Int,
    key: Any?,
    offset: Int?,
    limit: Int,
    iterator: QueryIterator?,
  ): ResultSet

  suspend fun select(
    space: String,
    index: String,
    key: Any?,
    offset: Int?,
    limit: Int,
    iterator: QueryIterator?,
  ): ResultSet

  suspend fun call(
    function: String,
    vararg args: Any?,
  ): Any?

  suspend fun call(
    function: String,
    args: List<Any?>,
  ): Any?

  suspend fun eval(
    lua: String,
    vararg args: Any?,
  ): Any?

  suspend fun eval(
    lua: String,
    args: List<Any?>,
  ): Any?

  suspend fun sql(
    sql: String,
    args: List<Any?> = emptyList(),
  ): ResultSet

  suspend fun sql(
    stm: TarantoolStatement,
    args: List<Any?> = emptyList(),
  ): ResultSet

  suspend fun prepare(sql: String): TarantoolStatement
}
