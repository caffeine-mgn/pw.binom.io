package pw.binom.db.tarantool

import pw.binom.*
import pw.binom.concurrency.ThreadRef
import pw.binom.db.tarantool.protocol.*
import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteArrayOutput
import pw.binom.io.ClosedException
import pw.binom.io.IOException
import pw.binom.io.socket.SocketClosedException
import pw.binom.io.socket.nio.SocketNIOManager
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

private const val VSPACE_ID = 281
private const val VSPACE_ID_INDEX_ID = 0

private const val VINDEX_ID = 289
private const val VINDEX_ID_INDEX_ID = 0

class TarantoolConnection private constructor(private val networkThread: ThreadRef, con: SocketNIOManager.ConnectionRaw) : AsyncCloseable {
    companion object {
        suspend fun connect(manager: SocketNIOManager, host: String, port: Int, user: String?, password: String?): TarantoolConnection {
            val con = manager.connect(
                    host = host,
                    port = port
            )
            val buf = ByteBuffer.alloc(64)
            try {
                con.readFully(buf)
                buf.flip()
                val version = buf.asUTF8String().trim().substring(10)
                buf.clear()
                con.readFully(buf)
                buf.flip()
                val salt = buf.asUTF8String().trim()
//                println("salt: [$salt]")
                //TODO Добавить авторизацию
                val connection = TarantoolConnection(manager.threadRef, con)
                if (user != null && password != null) {
                    val data = connection.sendReceive(Code.AUTH, null, InternalProtocolUtils.buildAuthPacketData(user, password, salt))
                    println("data: $data")
                    data.assertException()
                }

                return connection
            } finally {
                buf.close()
            }
        }
    }

    private var syncCursor = 0L
    private val requests = HashMap<Long, Continuation<Package>>()
    private val connectionReference = con
    private var meta: List<TarantoolSpaceMeta> = emptyList()
    private var schemaVersion = 0

    private fun checkThread() {
        if (!networkThread.same) {
            throw IllegalStateException("You must call Connector method from network thread")
        }
    }

    private suspend fun loadMeta(): List<TarantoolSpaceMeta> {
        metaUpdating = true
        try {
            while (true) {
                val index = select<Any?>(VINDEX_ID, VINDEX_ID_INDEX_ID, emptyList<Int>(), 0, Int.MAX_VALUE, QueryIterator.ALL)
                val spaces = select<Any?>(VSPACE_ID, VSPACE_ID_INDEX_ID, emptyList<Int>(), 0, Int.MAX_VALUE, QueryIterator.ALL)
                if (index.second != spaces.second) {
                    continue
                }
                schemaVersion = index.second
                val indexesBySpace = index.first.map { it as List<Any?> }.groupBy { (it[0] as Number).toInt() }
                return spaces.first.map { it as List<Any?> }.map {
                    val id = (it[VSPACE_ID_FIELD_NUMBER] as Number).toInt()
                    val indexes = indexesBySpace.getOrElse(id) { emptyList() }
                    TarantoolSpaceMeta.create(it, indexes)
                }
            }
        } finally {
            metaUpdating = false
        }
    }

    private var metaUpdating = false
    suspend fun getMeta(): List<TarantoolSpaceMeta> {
        invalidateSchema()
        return meta
    }

    internal suspend fun invalidateSchema() {
        if (schemaVersion == 0) {
            meta = loadMeta()
        }
    }

    init {
        neverFreeze()
    }

    private val out = ByteArrayOutput()
    internal suspend fun sendReceive(code: Code, schemaId: Int? = null, body: Map<Any, Any?>): Package {
        checkThread()
        val sync = syncCursor++
        return run {
            try {
                val headers = HashMap<Int, Any?>()
                headers[Key.CODE.id] = code.id
                headers[Key.SYNC.id] = sync
                if (schemaId != null) {
                    headers[Key.SCHEMA_ID.id] = schemaId
                }
                InternalProtocolUtils.buildMessagePackage(
                        header = headers,
                        data = body,
                        out = out
                )
                out.data.position = 0
                out.data.limit = out.size
                connectionReference.write(out.data)
                val v = suspendCoroutine<Package> {
                    requests[sync] = it
                }
                if (!metaUpdating) {
                    v.header[Key.SCHEMA_ID.id]?.let {
                        if (it != schemaVersion) {
                            schemaVersion = 0
                        }
                    }
                }
                v
            } finally {
                out.clear()
                if (out.data.capacity > 1024) {
                    out.trimToSize()
                }
            }
        }
    }

    private var closed = false

    override suspend fun close() {
        checkThread()
        if (closed) {
            throw ClosedException()
        }
        closed = true
        connectionReference.close()
    }

    init {
        async2 {
            val buf = ByteBuffer.alloc(8)
            try {
                val packageReader = AsyncInputWithCounter(connectionReference)
                while (!closed) {
                    val vv = connectionReference.readByte(buf).toUByte()
                    if (vv != 0xCE.toUByte()) {
                        throw IOException("Invalid Protocol Header Response")
                    }
                    val size = connectionReference.readInt(buf)
                    buf.clear()
                    packageReader.limit = size
                    val msg = InternalProtocolUtils.unpack(buf, packageReader)
                    val headers = msg as Map<Int, Any?>
                    val body = if (packageReader.limit > 0) {
                        InternalProtocolUtils.unpack(buf, packageReader) as Map<Int, Any?>
                    } else
                        emptyMap()
                    val serial = headers[Key.SYNC.id] as Long? ?: throw IOException("Can't find serial of message")
                    val pkg = Package(
                            header = headers,
                            body = body
                    )
                    requests.remove(serial)?.resumeWith(Result.success(pkg))
                }
            } catch (e: SocketClosedException) {
                //NOP
            } catch (e: ClosedException) {
                //NOP
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                buf.close()
            }
        }
    }

    suspend fun prepare(sql: String): TarantoolStatement {
        val d = sendReceive(
                code = Code.PREPARE,
                body = mapOf(
                        Key.SQL_TEXT.id to sql
                )
        )

        d.assertException()
        return TarantoolStatement(d.body[Key.STMT_ID.id] as Int)
    }

    suspend fun sql(stm: TarantoolStatement, args: List<Any?> = emptyList()): ResultSet {
        invalidateSchema()
        val result = this.sendReceive(
                code = Code.EXECUTE,
                body = mapOf(
                        Key.STMT_ID.id to stm.id,
                        Key.SQL_BIND.id to args
                ),
                schemaId = schemaVersion
        )

        result.assertException()
        return ResultSet(result.body)
    }

    suspend fun sql(sql: String, args: List<Any?> = emptyList()): ResultSet {
        invalidateSchema()
        val result = this.sendReceive(
                code = Code.EXECUTE,
                body = mapOf(
                        Key.SQL_TEXT.id to sql,
                        Key.SQL_BIND.id to args
                ),
                schemaId = schemaVersion
        )

        result.assertException()
        return ResultSet(result.body)
    }

    suspend fun eval(lua: String, args: List<Any?> = emptyList()): Any? {
        invalidateSchema()
        val result = this.sendReceive(
                code = Code.EVAL,
                body = mapOf(
                        Key.EXPRESSION.id to lua,
                        Key.TUPLE.id to args
                ),
                schemaId = schemaVersion
        )


        println("Header: ${result.header}")
        println("Body: ${result.body}")
        result.assertException()
        return result.data
    }

    suspend fun call(function: String, args: List<Any?>) {
        val result = this.sendReceive(
                code = Code.CALL,
                body = mapOf(
                        Key.FUNCTION.id to function,
                        Key.TUPLE.id to args
                )
        )

        result.assertException()
        println("Header: ${result.header}")
        println("Body: ${result.body}")
        val body = result.body
        val err = body[Key.ERROR.id] as String?
        if (err != null) {
            throw RuntimeException("Tarantool Exception: $err")
        }
    }

    suspend fun ping() {
        sendReceive(
                code = Code.PING,
                body = emptyMap()
        ).assertException()
    }
}