package pw.binom.db.tarantool

import pw.binom.ByteBuffer
import pw.binom.alloc
import pw.binom.asUTF8String
import pw.binom.db.tarantool.protocol.Code
import pw.binom.db.tarantool.protocol.InternalProtocolUtils
import pw.binom.db.tarantool.protocol.QueryIterator
import pw.binom.io.AsyncCloseable
import pw.binom.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkCoroutineDispatcher

interface TarantoolConnection : AsyncCloseable {
    companion object {
        suspend fun connect(
            manager: NetworkCoroutineDispatcher = Dispatchers.Network,
            address: NetworkAddress,
            userName: String?,
            password: String?
        ): TarantoolConnectionImpl {
            println("TarantoolConnection #1 address: $address")
            val con = manager.tcpConnect(address)
            println("TarantoolConnection #2")
            ByteBuffer.alloc(64) { buf ->
                var connection: TarantoolConnectionImpl? = null
                println("TarantoolConnection #3")
                try {
                    println("TarantoolConnection #3")
                    con.readFully(buf)
                    println("TarantoolConnection #4")
                    buf.flip()
                    val version = buf.asUTF8String().trim().substring(10)
                    buf.clear()
                    con.readFully(buf)
                    buf.flip()
                    val salt = buf.asUTF8String().trim()
                    println("TarantoolConnection #5")
                    connection = TarantoolConnectionImpl(
//                        networkThread = ThreadRef(),
                        networkDispatcher = manager,
                        con = con,
                        serverVersion = version
                    )
                    println("TarantoolConnection #6")
                    connection.mainLoopJob = GlobalScope.launch(manager) { connection.startMainLoop() }
                    println("TarantoolConnection #7")
                    if (userName != null && password != null) {
                        connection.sendReceive(
                            code = Code.AUTH,
                            schemaId = null,
                            body = InternalProtocolUtils.buildAuthPacketData(userName, password, salt)
                        ).assertException()
                    }
                    println("TarantoolConnection #8")
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
        values: List<Any?>
    )

    suspend fun insert(
        space: String,
        values: List<Any?>
    )

    suspend fun delete(
        space: Int,
        keys: List<Any?>
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
        values: List<FieldUpdate>
    ): Row?

    suspend fun update(
        space: String,
        key: List<Any?>,
        values: List<FieldUpdate>
    ): Row?

    suspend fun replace(
        space: Int,
        values: List<Any?>
    )

    suspend fun replace(
        space: String,
        values: List<Any?>
    )

    suspend fun delete(
        space: String,
        keys: List<Any?>
    ): Row?

    suspend fun select(
        space: Int,
        index: Int,
        key: Any?,
        offset: Int?,
        limit: Int,
        iterator: QueryIterator?
    ): ResultSet

    suspend fun select(
        space: String,
        index: String,
        key: Any?,
        offset: Int?,
        limit: Int,
        iterator: QueryIterator?
    ): ResultSet

    suspend fun call(function: String, vararg args: Any?): Any?

    suspend fun call(function: String, args: List<Any?>): Any?

    suspend fun eval(lua: String, vararg args: Any?): Any?
    suspend fun eval(lua: String, args: List<Any?>): Any?
    suspend fun sql(sql: String, args: List<Any?> = emptyList()): ResultSet
    suspend fun sql(stm: TarantoolStatement, args: List<Any?> = emptyList()): ResultSet
    suspend fun prepare(sql: String): TarantoolStatement
}