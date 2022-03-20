package pw.binom.db.radis

import pw.binom.ByteBuffer
import pw.binom.atomic.AtomicBoolean
import pw.binom.io.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * https://redis.io/commands
 */
class RadisConnectionImpl(val connection: AsyncChannel) : RadisConnection {
    private var version: String? = null
    private var majorVersion = 0
    private val resp = RESP3Impl(
        output = connection,
        input = connection,
        closeParent = false
    )

    internal suspend fun start() {
//        val info = info()
//        val version = info["redis_version"]
//        this.version = version
//        if (version != null) {
//            val pointIndex = version.indexOf('.')
//            if (pointIndex > 0) {
//                majorVersion = version.substring(0, pointIndex).toIntOrNull() ?: 0
//            }
//        }
    }

    suspend fun ping() {
        checkClosed()
        println("before!")
        operation {
            resp.startList(1)
            resp.writeASCIStringFast("PING")
            resp.flush()
            println("try read string....")
            val resp = resp.readString() ?: throw RadisException("Ping response is null")
            println("string: $resp")
            if (resp != "PONG") {
                throw RadisException("Invalid ping response \"$resp\"")
            }
        }
    }

    private val closed = AtomicBoolean(false)
    private fun checkClosed() {
        if (closed.value) {
            throw ClosedException()
        }
    }

    private val busy = AtomicBoolean(false)

    private inline fun <T> operation(func: () -> T): T {
        if (!busy.compareAndSet(false, true)) {
            throw IllegalStateException("Connection is busy")
        }
        try {
            return func()
        } finally {
            busy.value = false
        }
    }

    override val readyForRequest: Boolean
        get() = !busy.value

    override suspend fun asyncClose() {
        if (!closed.compareAndSet(false, true)) {
            throw ClosedException()
        }
        resp.asyncClose()
        connection.asyncClose()
    }

    suspend fun lset(key: String, value: String, index: Int) {
        operation {
            resp.startList(4)
            resp.writeASCIStringFast("LSET")
            resp.writeASCIStringFast(key)
            resp.writeLong(index)
            resp.writeString(value)
            resp.flush()
            checkOkResponse()
        }
    }

    suspend fun info(): Map<String, String> {
        operation {
            println("getting info")
            resp.startList(1)
            resp.writeASCIStringFast("INFO")
            resp.flush()
            println("request sent. reading response")
            val text = resp.readString()
            println("text: $text")
            val cc = text!!.lineSequence()
                .filter { !it.startsWith("#") }
                .map {
                    val items = it.split(':', limit = 2)
                    items[0] to items.getOrElse(1) { "" }
                }.toMap()
            println("->$cc")
            return cc
        }
    }

    suspend fun isKeyExist(key: String): Long {
        operation {
            resp.startList(2)
            resp.writeASCIStringFast("EXISTS")
            resp.writeASCIStringFast(key)
            resp.flush()
            return resp.readLong() ?: 0L
        }
    }

    suspend fun removeTimeoutFromKey(key: String): Long {
        operation {
            resp.startList(2)
            resp.writeASCIStringFast("PERSIST")
            resp.writeASCIStringFast(key)
            resp.flush()
            return resp.readLong()!!
        }
    }

    suspend fun renameKey(oldName: String, newName: String) {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("RENAME")
            resp.writeASCIStringFast(oldName)
            resp.writeASCIStringFast(newName)
            resp.flush()
            checkOkResponse()
        }
    }

    suspend fun substring(key: String, start: Int = 0, end: Int = -1): String? {
        operation {
            resp.startList(4)
            resp.writeASCIStringFast("GETRANGE")
            resp.writeASCIStringFast(key)
            resp.writeLong(start)
            resp.writeLong(end)
            resp.flush()
            return resp.readString()
        }
    }

    suspend fun replaceString(key: String, position: Int, value: String): Long? {
        operation {
            resp.startList(4)
            resp.writeASCIStringFast("SETRANGE")
            resp.writeASCIStringFast(key)
            resp.writeLong(position)
            resp.writeString(value)
            resp.flush()
            return resp.readLong()
        }
    }

    suspend fun appendString(key: String, value: String): Long? {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("APPEND")
            resp.writeASCIStringFast(key)
            resp.writeString(value)
            resp.flush()
            return resp.readLong()
        }
    }

    suspend fun appendStringAsByteArray(key: String, data: ByteArray): Long {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("APPEND")
            resp.writeASCIStringFast(key)
            resp.writeDataString(data)
            resp.flush()
            return resp.readLong()!!
        }
    }

    suspend fun appendStringAsByteArray(key: String, data: ByteBuffer): Long {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("APPEND")
            resp.writeASCIStringFast(key)
            resp.writeDataString(data)
            resp.flush()
            return resp.readLong()!!
        }
    }

    suspend fun insertFirst(key: String, value: String): Long {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("LPUSH")
            resp.writeASCIStringFast(key)
            resp.writeString(value)
            resp.flush()
            return resp.readLong()!!
        }
    }

    suspend fun insertFirstOnlyOfExist(key: String, value: String) {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("LPUSH")
            resp.writeASCIStringFast(key)
            resp.writeString(value)
            resp.flush()
            checkOkResponse()
        }
    }

    suspend fun insertLast(key: String, value: String) {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("RPUSH")
            resp.writeASCIStringFast(key)
            resp.writeString(value)
            resp.flush()
            checkOkResponse()
        }
    }

    suspend fun duplicateOnFirst(key: String, value: String) {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("LPUSHX")
            resp.writeASCIStringFast(key)
            resp.writeString(value)
            resp.flush()
            checkOkResponse()
        }
    }

    suspend fun duplicateOnLast(key: String, value: String) {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("RPUSHX")
            resp.writeASCIStringFast(key)
            resp.writeString(value)
            resp.flush()
            checkOkResponse()
        }
    }

    suspend fun pttl(key: String): Duration? {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("PTTL")
            resp.writeASCIStringFast(key)
            resp.flush()
            val time = resp.readLong() ?: return null
            return time.milliseconds
        }
    }

    suspend fun ttl(key: String): Duration? {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("TTL")
            resp.writeASCIStringFast(key)
            resp.flush()
            val time = resp.readLong() ?: return null
            return time.seconds
        }
    }

    suspend fun pexpire(key: String, duration: Duration) {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("PEXPIRE")
            resp.writeASCIStringFast(key)
            resp.writeLong(duration.inWholeMilliseconds)
            resp.flush()
            checkOkResponse()
        }
    }

    suspend fun expire(key: String, duration: Duration) {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("EXPIRE")
            resp.writeASCIStringFast(key)
            resp.writeLong(duration.inWholeSeconds)
            resp.flush()
            checkOkResponse()
        }
    }

    suspend fun getKeyType(key: String): RadisConnection.ValueType? {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("TYPE")
            resp.writeASCIStringFast(key)
            resp.flush()
            val type = resp.readString() ?: return null

            return when (type) {
                "string" -> RadisConnection.ValueType.STRING
                "list" -> RadisConnection.ValueType.LIST
                "set" -> RadisConnection.ValueType.SET
                "zset" -> RadisConnection.ValueType.ZSET
                "hash" -> RadisConnection.ValueType.HASH
                "stream" -> RadisConnection.ValueType.STREAM
                else -> throw RadisException("Unknown key type \"$type\"")
            }
        }
    }

    suspend fun updateLastAccess(vararg keys: String): Long {
        operation {
            resp.startList(keys.size + 1)
            resp.writeASCIStringFast("TOUCH")
            keys.forEach {
                resp.writeASCIStringFast(it)
            }
            return resp.readLong() ?: 0L
        }
    }

    suspend fun updateLastAccess(keys: List<String>): Long {
        operation {
            resp.startList(keys.size + 1)
            resp.writeASCIStringFast("TOUCH")
            keys.forEach {
                resp.writeASCIStringFast(it)
            }
            return resp.readLong() ?: 0L
        }
    }

    /**
     * Searching indexes in list [key] value with [match] value
     * @param rank start index of result for return
     * @param count count of response. [count]=0 - all results
     */
    suspend fun indexOfInList(key: String, match: String, rank: Int? = null, count: Int? = 1): List<Long> {
        operation {
            var c = 3
            if (rank != null)
                c += 2
            if (count != null) {
                c += 2
            }
            resp.startList(c)
            resp.writeASCIStringFast("LPOS")
            resp.writeASCIStringFast(key)
            resp.writeString(match)
            if (rank != null) {
                resp.writeASCIStringFast("RANK")
                resp.writeLong(rank)
            }
            if (count != null) {
                resp.writeASCIStringFast("COUNT")
                resp.writeLong(count)
            }
            resp.flush()
            return when (val indexResponse = resp.readResponse()) {
                null -> emptyList()
                is Long -> listOf(indexResponse)
                is List<*> -> indexResponse as List<Long>
                else -> TODO()
            }
        }
    }

    suspend fun getList(key: String, start: Int = 0, end: Int = -1): List<String>? {
        operation {
            resp.startList(4)
            resp.writeASCIStringFast("LRANGE")
            resp.writeASCIStringFast(key)
            resp.writeLong(start)
            resp.writeLong(end)
            return resp.readList() as List<String>?
        }
    }

    suspend fun getListSize(key: String): Long? {
        operation {
            resp.startList(2)
            resp.writeASCIStringFast("LLEN")
            resp.writeASCIStringFast(key)
            resp.flush()
            return resp.readLong()
        }
    }

    suspend fun setStringAsBytes(key: String, data: ByteArray) {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("SET")
            resp.writeASCIStringFast(key)
            resp.writeDataString(data)
            resp.flush()
            checkOkResponse()
        }
    }

    suspend fun setStringAsBytes(key: String, data: ByteBuffer) {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("SET")
            resp.writeASCIStringFast(key)
            resp.writeDataString(data)
            resp.flush()
            checkOkResponse()
        }
    }

    suspend fun setString(key: String, value: String) {
        operation {
            resp.startList(3)
            resp.writeASCIStringFast("SET")
            resp.writeASCIStringFast(key)
            resp.writeString(value)
            resp.flush()
            checkOkResponse()
        }
    }

    suspend fun getString(key: String): String? {
        operation {
            resp.startList(2)
            resp.writeASCIStringFast("GET")
            resp.writeASCIStringFast(key)
            resp.flush()
            return resp.readString() as String?
        }
    }

    suspend fun getStringAsByteArray(key: String): ByteArray? {
        var r: ByteArray? = null
        getStringAsByteBuffer(key) {
            r = it.toByteArray()
        }
        return r
    }

    suspend fun getStringAsByteBuffer(key: String, func: suspend (ByteBuffer) -> Unit): Boolean {
        operation {
            resp.startList(2)
            resp.writeASCIStringFast("GET")
            resp.flush()
            return resp.readStringDataByteBuffer(func)
        }
    }

    private suspend fun checkOkResponse() {
        val resp = resp.readString()
        if (resp != "OK") {
            throw RadisException("Invalid response $resp")
        }
    }
}
