package pw.binom.db.tarantool

import pw.binom.*
import pw.binom.concurrency.ThreadRef
import pw.binom.db.tarantool.protocol.*
import pw.binom.io.AsyncCloseable
import pw.binom.io.ByteArrayOutput
import pw.binom.io.ClosedException
import pw.binom.io.IOException
import pw.binom.network.NetworkAddress
import pw.binom.network.NetworkDispatcher
import pw.binom.network.SocketClosedException
import pw.binom.network.TcpConnection
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

private const val VSPACE_ID = 281
private const val VSPACE_ID_INDEX_ID = 0

private const val VINDEX_ID = 289
private const val VINDEX_ID_INDEX_ID = 0

class TarantoolConnection private constructor(private val networkThread: ThreadRef, con: TcpConnection) :
    AsyncCloseable {
    companion object {
        suspend fun connect(
            manager: NetworkDispatcher,
            address: NetworkAddress,
            userName: String?,
            password: String?
        ): TarantoolConnection {
            val con = manager.tcpConnect(address)
            val buf = ByteBuffer.alloc(64)
            try {
                con.readFully(buf)
                buf.flip()
                val version = buf.asUTF8String().trim().substring(10)
                buf.clear()
                con.readFully(buf)
                buf.flip()
                val salt = buf.asUTF8String().trim()
                val connection = TarantoolConnection(ThreadRef(), con)
                if (userName != null && password != null) {
                    connection.sendReceive(
                        Code.AUTH,
                        null,
                        InternalProtocolUtils.buildAuthPacketData(userName, password, salt)
                    ).assertException()
                }
                return connection
            } catch (e: Throwable) {
                con.close()
                throw e
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
                val index =
                    select1<Any?>(VINDEX_ID, VINDEX_ID_INDEX_ID, emptyList<Int>(), 0, Int.MAX_VALUE, QueryIterator.ALL)
                val spaces =
                    select1<Any?>(VSPACE_ID, VSPACE_ID_INDEX_ID, emptyList<Int>(), 0, Int.MAX_VALUE, QueryIterator.ALL)
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

    override suspend fun asyncClose() {
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
                    if (vv != 0xce.toUByte()) {
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
                asyncClose()
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

    suspend fun eval(lua: String, args: List<Any?>): Any? {
        invalidateSchema()
        val result = this.sendReceive(
            code = Code.EVAL,
            body = mapOf(
                Key.EXPRESSION.id to lua,
                Key.TUPLE.id to args
            ),
            schemaId = schemaVersion
        )
        result.assertException()
        return result.data
    }

    suspend fun eval(lua: String, vararg args: Any?): Any? {
        invalidateSchema()
        val result = this.sendReceive(
            code = Code.EVAL,
            body = mapOf(
                Key.EXPRESSION.id to lua,
                Key.TUPLE.id to args
            ),
            schemaId = schemaVersion
        )
        result.assertException()
        return result.data
    }


    suspend fun call(function: String, args: List<Any?>): Any? {
        val result = this.sendReceive(
            code = Code.CALL,
            body = mapOf(
                Key.FUNCTION.id to function,
                Key.TUPLE.id to args
            )
        )
        result.assertException()
        return result.data
    }

    suspend fun call(function: String, vararg args: Any?): Any? {
        val result = this.sendReceive(
            code = Code.CALL,
            body = mapOf(
                Key.FUNCTION.id to function,
                Key.TUPLE.id to args
            )
        )
        result.assertException()
        return result.data
    }

    suspend fun select(
        space: String,
        index: String,
        key: Any?,
        offset: Int?,
        limit: Int,
        iterator: QueryIterator?
    ): ResultSet {
        val meta = getMeta()
        val spaceObj = meta.find { it.name == space } ?: throw TarantoolException("Can't find Space \"$space\"")
        val indexObj = spaceObj.indexes[index] ?: throw TarantoolException("Can't find Index \"$space\".\"$index\"")
        return select(
            space = spaceObj.id,
            index = indexObj.id,
            key = key,
            offset = offset,
            limit = limit,
            iterator = iterator
        )
    }

    suspend fun select(
        space: Int,
        index: Int,
        key: Any?,
        offset: Int?,
        limit: Int,
        iterator: QueryIterator?
    ): ResultSet {
        val body = HashMap<Any, Any?>()
        body[Key.SPACE.id] = space
        body[Key.INDEX.id] = index
        body[Key.LIMIT.id] = limit
        body[Key.KEY.id] = key ?: listOf<Any>()
        if (iterator != null) {
            body[Key.ITERATOR.id] = iterator.value
        }
        if (offset != null) {
            body[Key.OFFSET.id] = offset
        }
        val result = this.sendReceive(
            code = Code.SELECT,
            body = body
        )
        result.assertException()
        return ResultSet(result.body)
    }

    suspend fun delete(
        space: String,
        keys: List<Any?>
    ): List<List<Any?>> {
        val meta = getMeta()
        val spaceObj = meta.find { it.name == space } ?: throw TarantoolException("Can't find Space \"$space\"")
        return delete(
            space = spaceObj.id,
            keys = keys
        )
    }

    suspend fun replace(
        space: String,
        values: List<Any?>
    ) {
        val meta = getMeta()
        val spaceObj = meta.find { it.name == space } ?: throw TarantoolException("Can't find Space \"$space\"")
        replace(
            space = spaceObj.id,
            values = values
        )
    }

    suspend fun replace(
        space: Int,
        values: List<Any?>
    ) {

        val result = this.sendReceive(
            code = Code.REPLACE,
            body = mapOf(
                Key.SPACE.id to space,
                Key.TUPLE.id to values
            )
        )
        result.assertException()
    }

    suspend fun update(
        space: String,
        key: List<Any?>,
        values: List<FieldUpdate>
    ): Row? {
        val meta = getMeta()
        val spaceObj = meta.find { it.name == space } ?: throw TarantoolException("Can't find Space \"$space\"")
        return update(
            space = spaceObj.id,
            key = key,
            values = values,
        )
    }

    suspend fun update(
        space: Int,
        key: List<Any?>,
        values: List<FieldUpdate>
    ): Row? {

        val result = this.sendReceive(
            code = Code.UPDATE,
            body = mapOf(
                Key.SPACE.id to space,
                Key.KEY.id to key,
                Key.TUPLE.id to values.map {
                    listOf(it.operator.code, it.fieldId, it.value)
                },
            )
        )
        result.assertException()
        return ResultSet(result.body).firstOrNull()
    }

//    suspend fun upsert(
//            space: String,
//            key: Any?,
//            values: List<Any?>) {
//        val meta = getMeta()
//        val spaceObj = meta.find { it.name == space } ?: throw TarantoolException("Can't find Space \"$space\"")
//        upsert(
//                space = spaceObj.id,
//                key = key,
//                values = values,
//        )
//    }

//    suspend fun upsert(
//            space: Int,
//            key: Any?,
//            values: List<Any?>) {
//
//        val result = this.sendReceive(
//                code = Code.UPSERT,
//                body = mapOf(
//                        Key.SPACE.id to space,
//                        Key.KEY.id to key,
//                        Key.TUPLE.id to values,
//                )
//        )
//        println("Update columns: ${result.body}")
//        result.assertException()
//    }

    suspend fun delete(
        space: Int,
        keys: List<Any?>
    ): List<List<Any?>> {

        val result = this.sendReceive(
            code = Code.DELETE,
            body = mapOf(
                Key.SPACE.id to space,
                Key.KEY.id to keys
            )
        )
        result.assertException()
        return result.data as List<List<Any?>>
    }

    suspend fun insert(
        space: String,
        values: List<Any?>
    ) {
        val meta = getMeta()
        val spaceObj = meta.find { it.name == space } ?: throw TarantoolException("Can't find Space \"$space\"")
        insert(
            space = spaceObj.id,
            values = values,
        )
    }

    suspend fun insert(
        space: Int,
        values: List<Any?>
    ) {

        val result = this.sendReceive(
            code = Code.INSERT,
            body = mapOf(
                Key.SPACE.id to space,
                Key.TUPLE.id to values
            )
        )
        result.assertException()
    }

    suspend fun ping() {
        sendReceive(
            code = Code.PING,
            body = emptyMap()
        ).assertException()
    }
}