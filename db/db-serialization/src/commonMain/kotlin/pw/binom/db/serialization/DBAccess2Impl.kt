package pw.binom.db.serialization

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import pw.binom.UUID
import pw.binom.collections.defaultMutableList
import pw.binom.collections.defaultMutableMap
import pw.binom.date.DateTime
import pw.binom.db.ResultSet
import pw.binom.db.SQLException
import pw.binom.db.async.pool.PooledAsyncConnection
import pw.binom.io.use

private class SingleValueDateContainer : DateContainer {
    var value: Any? = null
    var useQuotes: Boolean = false
    override fun set(key: String, value: Any?, useQuotes: Boolean) {
        this.value = value
        this.useQuotes = useQuotes
    }
}

class ResultSetDataProvider(val rs: ResultSet) : DataProvider {
    override fun get(key: String): Any? = rs.getString(key)
    private fun columnNotDefined(key: String): Nothing = throw SQLException("Column \"$key\" is null")
    override fun getString(key: String): String = rs.getString(key) ?: columnNotDefined(key)

    override fun getBoolean(key: String): Boolean = rs.getBoolean(key) ?: columnNotDefined(key)

    override fun isNull(key: String): Boolean = rs.isNull(key)

    override fun getInt(key: String): Int = rs.getInt(key) ?: columnNotDefined(key)

    override fun getLong(key: String): Long = rs.getLong(key) ?: columnNotDefined(key)

    override fun getFloat(key: String): Float = rs.getFloat(key) ?: columnNotDefined(key)

    override fun getDouble(key: String): Double = rs.getDouble(key) ?: columnNotDefined(key)

    override fun getShort(key: String): Short = getInt(key).toShort()

    override fun getByte(key: String): Byte = getInt(key).toByte()

    override fun getByteArray(key: String): ByteArray = rs.getBlob(key) ?: columnNotDefined(key)

    override fun getChar(key: String): Char {
        val s = rs.getString(key) ?: columnNotDefined(key)
        if (s.length != 0) {
            throw SQLException("Column \"$key\" has invalid char")
        }
        return s[0]
    }

    override fun getUUID(key: String): UUID = rs.getUUID(key) ?: columnNotDefined(key)

    override fun getDateTime(key: String): DateTime = rs.getDate(key) ?: columnNotDefined(key)

    override fun contains(key: String): Boolean {
        val kk = key.lowercase()
        return rs.columns.find { it.lowercase() == kk } != null
    }
}

private class QueryContextImpl(override val serializersModule: SerializersModule) : UpdateContext {
    var args = defaultMutableList<Any?>()
    var returning: Returning<out Any>? = null

    class Returning<T : Any>(val serializer: KSerializer<T>, val func: suspend (T) -> Unit)

    override fun <T : Any> returning(k: KSerializer<T>, func: suspend (T) -> Unit) {
        if (returning != null) {
            throw IllegalStateException("Returning function already defined")
        }
        returning = Returning(
            serializer = k,
            func = func
        )
    }

    override fun <T : Any> param(k: KSerializer<T>, value: T?): String {
        if (value == null) {
            args += null
        } else {
            val singleValueDateContainer = SingleValueDateContainer()
            DefaultSQLSerializePool.encode(
                serializer = k,
                value = value,
                name = "",
                output = singleValueDateContainer,
                serializersModule = serializersModule,
                useQuotes = false,
                excludeGenerated = false,
            )
            args += singleValueDateContainer.value
        }
        return "?"
    }
}

class DBAccess2Impl(val con: PooledAsyncConnection, val serializersModule: SerializersModule) : DBAccess2 {

    override suspend fun <T : Any> insert(k: KSerializer<T>, value: T, excludeGenerated: Boolean) {
        insert2(k = k, value = value, returning = false, excludeGenerated = excludeGenerated)
    }

    override suspend fun <T : Any> insertAndReturn(k: KSerializer<T>, value: T, excludeGenerated: Boolean): T =
        insert2(k = k, value = value, returning = true, excludeGenerated = excludeGenerated)
            ?: throw IllegalStateException("Can't extract returned inserted value")

    private suspend inline fun <T : Any> insert2(
        k: KSerializer<T>,
        value: T,
        returning: Boolean,
        excludeGenerated: Boolean
    ): T? {
        val params = defaultMutableMap<String, Pair<Boolean, Any?>>()
        val output = object : DataBinder {
            override fun get(key: String): Any? = params[key]
            override fun contains(key: String): Boolean = params.containsKey(key)
            override fun set(key: String, value: Any?, useQuotes: Boolean) {
                params[key] = useQuotes to value
            }
        }
        DefaultSQLSerializePool.encode(
            serializer = k,
            value = value,
            name = "",
            output = output,
            serializersModule = serializersModule,
            useQuotes = k.descriptor.isUseQuotes(),
            excludeGenerated = excludeGenerated,
        )
        val sb = StringBuilder()
        sb.append("insert into ")
            .append(getTableName(k.descriptor))
            .append("(")
        var i = 0
        params.forEach {
            if (i > 0) {
                sb.append(",")
            }
            val useQuotes = it.value.first
            if (useQuotes) {
                sb.append("\"")
            }
            sb.append(it.key)
            if (useQuotes) {
                sb.append("\"")
            }
            i++
        }
        i = 0
        sb.append(") values(")
        val args = arrayOfNulls<Any>(params.size)
        params.forEach {
            if (i > 0) {
                sb.append(",")
            }
            sb.append("?")
            args[i++] = it.value.second
        }
        sb.append(")")
        if (returning) {
            sb.append(" returning ")
            i = 0
            params.forEach {
                if (i > 0) {
                    sb.append(",")
                }
                val useQuotes = it.value.first
                if (useQuotes) {
                    sb.append("\"")
                }
                sb.append(it.key)
                if (useQuotes) {
                    sb.append("\"")
                }
            }
        }
        val ps = con.usePreparedStatement(sb.toString())
        return if (returning) {
            return ps.executeQuery(*args).use { result ->
                val r = ResultSetDataProvider(result)
                if (result.next()) {
                    DefaultSQLSerializePool.decode(
                        serializer = k,
                        name = "",
                        input = r,
                        serializersModule = serializersModule,
                    )
                } else {
                    null
                }
            }
        } else {
            ps.executeUpdate(*args)
            null
        }
    }

    override suspend fun <T : Any> select(k: KSerializer<T>, func: suspend QueryContext.() -> String): Flow<T> {
        val params = QueryContextImpl(serializersModule)
        val query = func(params)
        val ps = con.usePreparedStatement(query)
        val result = ps.executeQuery(*params.args.toTypedArray())
        val r = ResultSetDataProvider(result)
        return flow {
            try {
                while (result.next()) {
                    val obj = DefaultSQLSerializePool.decode(
                        serializer = k,
                        name = "",
                        input = r,
                        serializersModule = serializersModule,
                    )
                    emit(obj)
                }
            } finally {
                result.asyncClose()
            }
        }
    }

    override suspend fun update(func: suspend QueryContext.() -> String): Long {
        val params = QueryContextImpl(serializersModule)
        val query = func(params)
        val ps = con.usePreparedStatement(query)
        return ps.executeUpdate(*params.args.toTypedArray())
    }

    override suspend fun <T : Any> selectAll(
        k: KSerializer<T>,
        condition: (suspend QueryContext.() -> String)?
    ): Flow<T> =
        select(k = k) {
            if (condition != null) {
                "select * from ${tableName(k.descriptor)} where ${condition(this)}"
            } else {
                "select * from ${tableName(k.descriptor)}"
            }
        }
}
