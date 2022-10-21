package pw.binom.db.tarantool

import kotlinx.coroutines.*
import pw.binom.atomic.AtomicLong
import pw.binom.collections.defaultMutableMap
import pw.binom.db.tarantool.protocol.*
import pw.binom.io.*
import pw.binom.network.NetworkManager
import pw.binom.network.SocketClosedException
import pw.binom.network.TcpConnection
import pw.binom.neverFreeze
import pw.binom.readByte
import pw.binom.readInt
import kotlin.collections.set
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val VSPACE_ID = 281
private const val VSPACE_ID_INDEX_ID = 0

private const val VINDEX_ID = 289
private const val VINDEX_ID_INDEX_ID = 0

@Suppress("UNCHECKED_CAST")
class TarantoolConnectionImpl internal constructor(
//    private val networkThread: ThreadRef,
    val networkDispatcher: NetworkManager,
    connection: TcpConnection,
    val serverVersion: String,
) :
    TarantoolConnection {

    internal var mainLoopJob: Job? = null
    private var syncCursor = AtomicLong(0)
    private val requests = defaultMutableMap<Long, CancellableContinuation<Package>>()
    private val connectionReference = connection
    private var meta: List<TarantoolSpaceMeta> = emptyList()
    private var schemaVersion = 0
    private var connected = true
    private var metaUpdating = false
    private val out = ByteArrayOutput()
    private var closed = false
    val isConnected
        get() = connected

//    private fun checkThread() {
//        if (!networkThread.same) {
//            throw IllegalStateException("You must call Connector method from network thread")
//        }
//    }

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
        } catch (e: SocketClosedException) {
            connected = false
            throw e
        } finally {
            metaUpdating = false
        }
    }

    override suspend fun getMeta(): List<TarantoolSpaceMeta> {
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

    internal suspend fun sendReceive(code: Code, schemaId: Int? = null, body: Map<Any, Any?>): Package {
        val sync = syncCursor.addAndGet(1)
        val headers = defaultMutableMap<Int, Any?>()
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
        return let { _ ->
            try {
                withContext(networkDispatcher) {
                    connectionReference.writeFully(out.data)
                }
                val response = suspendCancellableCoroutine<Package> {
                    requests[sync] = it
                    it.invokeOnCancellation {
                        requests.remove(sync)
                    }
                }
                if (!metaUpdating) {
                    response.header[Key.SCHEMA_ID.id]?.let {
                        if (it != schemaVersion) {
                            schemaVersion = 0
                        }
                    }
                }
                response
            } catch (e: SocketClosedException) {
                connected = false
                throw e
            } catch (e: Throwable) {
                throw e
            } finally {
                out.clear()
                if (out.data.capacity > 1024) {
                    out.trimToSize()
                }
            }
        }
    }

    override suspend fun asyncClose() {
        if (closed) {
            throw ClosedException()
        }
        try {
            mainLoopJob?.cancel()
        } catch (e: CancellationException) {
            // Do nothing
        }
        closed = true
        out.close()
        connectionReference.close()
    }

    internal suspend fun startMainLoop() {
        withContext(networkDispatcher) {
            ByteBuffer.alloc(8).use { buf ->
                try {
                    val packageReader = AsyncInputWithCounter(connectionReference)
                    while (mainLoopJob?.isActive != false && !closed) {
                        val f = connectionReference.readByte(buf)
                        val vv = f.toUByte()
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
                        } else {
                            emptyMap()
                        }
                        val serial = headers[Key.SYNC.id] as Long? ?: throw IOException("Can't find serial of message")
                        val pkg = Package(
                            header = headers,
                            body = body
                        )
                        requests.remove(serial)?.resume(pkg)
                    }
                } catch (e: SocketClosedException) {
                    e.printStackTrace()
                    requests.forEach {
                        it.value.resumeWithException(e)
                    }
                } catch (e: CancellationException) {
                    e.printStackTrace()
                    requests.forEach {
                        it.value.resumeWithException(e)
                    }
                } catch (e: ClosedException) {
                    e.printStackTrace()
                    requests.forEach {
                        it.value.resumeWithException(e)
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    e.printStackTrace()
                    requests.forEach {
                        it.value.resumeWithException(e)
                    }
                    asyncClose()
                } finally {
                    requests.clear()
                    connected = false
                }
            }
        }
    }

    override suspend fun prepare(sql: String): TarantoolStatement {
        val d = sendReceive(
            code = Code.PREPARE,
            body = mapOf(
                Key.SQL_TEXT.id to sql
            )
        )

        d.assertException()
        return TarantoolStatement(d.body[Key.STMT_ID.id] as Int)
    }

    override suspend fun sql(stm: TarantoolStatement, args: List<Any?>): ResultSet {
        invalidateSchema()
        val result = sendReceive(
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

    override suspend fun sql(sql: String, args: List<Any?>): ResultSet {
        invalidateSchema()
        val result = sendReceive(
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

    override suspend fun eval(lua: String, args: List<Any?>): Any? {
        invalidateSchema()
        val result = sendReceive(
            code = Code.EVAL,
            body = mapOf(
                Key.EXPRESSION.id to lua,
                Key.TUPLE.id to args
            ),
            schemaId = schemaVersion
        )
        if (result.isError) {
            throw TarantoolEvalException(script = lua, errorMessage = result.errorMessage)
        }
        return result.data
    }

    override suspend fun eval(lua: String, vararg args: Any?): Any? {
        invalidateSchema()
        val result = sendReceive(
            code = Code.EVAL,
            body = mapOf(
                Key.EXPRESSION.id to lua,
                Key.TUPLE.id to args
            ),
            schemaId = schemaVersion
        )
        if (result.isError) {
            throw TarantoolEvalException(script = lua, errorMessage = result.errorMessage)
        }
        return result.data
    }

    override suspend fun call(function: String, args: List<Any?>): Any? {
        val result = sendReceive(
            code = Code.CALL,
            body = mapOf(
                Key.FUNCTION.id to function,
                Key.TUPLE.id to args
            )
        )
        result.assertException()
        return result.data
    }

    override suspend fun call(function: String, vararg args: Any?): Any? {
        val result = sendReceive(
            code = Code.CALL,
            body = mapOf(
                Key.FUNCTION.id to function,
                Key.TUPLE.id to args
            )
        )
        result.assertException()
        return result.data
    }

    override suspend fun select(
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

    override suspend fun select(
        space: Int,
        index: Int,
        key: Any?,
        offset: Int?,
        limit: Int,
        iterator: QueryIterator?
    ): ResultSet {
        val body = defaultMutableMap<Any, Any?>()
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
        val result = sendReceive(
            code = Code.SELECT,
            body = body
        )
        result.assertException()
        return ResultSet(result.body)
    }

    override suspend fun delete(
        space: String,
        keys: List<Any?>
    ): Row? {
        val meta = getMeta()
        val spaceObj = meta.find { it.name == space } ?: throw TarantoolException("Can't find Space \"$space\"")
        return delete(
            space = spaceObj.id,
            keys = keys
        )
    }

    override suspend fun replace(
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

    override suspend fun replace(
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

    override suspend fun update(
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

    override suspend fun update(
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

    override suspend fun upsert(
        space: String,
        indexValues: List<Any?>,
        values: List<FieldUpdate>,
    ) {
        val meta = getMeta()
        val spaceObj = meta.find { it.name == space } ?: throw TarantoolException("Can't find Space \"$space\"")
        upsert(
            space = spaceObj.id,
            indexValues = indexValues,
            values = values,
        )
    }

    override suspend fun upsert(
        space: Int,
        indexValues: List<Any?>,
        values: List<FieldUpdate>,
    ) {
        val result = this.sendReceive(
            code = Code.UPSERT,
            body = mapOf(
                Key.SPACE.id to space,
                Key.UPSERT_OPS.id to values.map {
                    listOf(it.operator.code, it.fieldId, it.value)
                },
                Key.TUPLE.id to indexValues,
            )
        )
        result.assertException()
    }

    override suspend fun delete(
        space: Int,
        keys: List<Any?>
    ): Row? {
        val result = this.sendReceive(
            code = Code.DELETE,
            body = mapOf(
                Key.SPACE.id to space,
                Key.KEY.id to keys
            )
        )
        result.assertException()
        return ResultSet(result.body).firstOrNull()
    }

    override suspend fun insert(
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

    override suspend fun insert(
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

    override suspend fun ping() {
        sendReceive(
            code = Code.PING,
            body = emptyMap()
        ).assertException()
    }
}
