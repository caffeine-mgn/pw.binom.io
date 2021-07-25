package pw.binom.db.serialization

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import pw.binom.UUID
import pw.binom.date.Calendar
import pw.binom.date.Date
import pw.binom.db.ResultSet
import pw.binom.db.async.pool.SelectQuery
import pw.binom.db.async.pool.SelectQueryWithMapper
import pw.binom.db.async.pool.UpdateQuery

val SqlSerializersModule = SerializersModule {
    this.contextual(UUID::class, UUIDSerializer)
    this.contextual(Date::class, DateSerializer)
    this.contextual(Calendar::class, CalendarSerializer)
}


fun <T : Any> KSerializer<T>.selectQuery(queryPart: String? = null): SelectQueryWithMapper<T> = run {
    val q = SQLSerialization.selectQuery(this)
    val txt = if (queryPart == null) {
        q
    } else {
        "$q $queryPart"
    }
    SelectQuery(
        txt
    ).mapper(SQLSerialization.DEFAULT.mapper(this))
}

class SQLSerialization(val serializersModule: SerializersModule = SqlSerializersModule) {
    companion object {
        val DEFAULT = SQLSerialization()
        fun <T : Any> getTableName(serializer: KSerializer<T>, tableName: String?): String {
            val descriptor = serializer.descriptor
            val ann = descriptor.annotations.asSequence().mapNotNull { it as? TableName }.firstOrNull()
            var result = tableName ?: ann?.tableName ?: descriptor.serialName
            return result
        }

        @OptIn(InternalSerializationApi::class)
        inline fun <reified T : Any> getTableName(tableName: String? = null) =
            getTableName(serializer = T::class.serializer(), tableName = tableName)

        fun <T : Any> selectQuery(
            serializer: KSerializer<T>,
            tableName: String? = null,
        ): String {
            val sb = StringBuilder()
            sb.append("SELECT ")
            val table = getTableName(serializer, tableName)
            val descriptor = serializer.descriptor
            repeat(descriptor.elementsCount) {
                if (it > 0) {
                    sb.append(", ")
                }
                sb.append(descriptor.getElementName(it))
            }
            sb.append(" FROM ").append(table)
            return sb.toString()
        }

        fun updateQuery(
            serializer: KSerializer<out Any>,
            tableName: String? = null,
            excludes: Set<String> = emptySet(),
            includes: Set<String> = emptySet(),
        ): String {
            val sb = StringBuilder()
            val descriptor = serializer.descriptor
            val table = getTableName(serializer, tableName = tableName)
            sb.append("UPDATE ").append(table).append(" SET ")
            var first = true
            repeat(descriptor.elementsCount) {
                val el = descriptor.getElementName(it)
                if ((includes.isNotEmpty() && el !in includes) || (el in excludes)) {
                    return@repeat
                }
                if (!first) {
                    sb.append(", ")
                }
                sb.append(el).append("=").append(":").append(el)
                first = false
            }
            if (first) {
                throw IllegalArgumentException("Can't generate update query. All field of ${serializer.descriptor.serialName} are excluded")
            }
            return sb.toString()
        }

        fun <T : Any> insertQuery(
            serializer: KSerializer<T>,
            tableName: String? = null,
        ): String {
            val sb = StringBuilder()
            val table = getTableName(serializer, tableName)
            val descriptor = serializer.descriptor
            sb.append("INSERT INTO ").append(table).append(" (")
            repeat(descriptor.elementsCount) {
                if (it > 0) {
                    sb.append(", ")
                }
                sb.append(descriptor.getElementName(it))
            }
            sb.append(") VALUES (")
            repeat(descriptor.elementsCount) {
                if (it > 0) {
                    sb.append(", ")
                }
                sb.append(":").append(descriptor.getElementName(it))
            }
            sb.append(")")
            return sb.toString()
        }
    }


    fun <T : Any> makeInsert(serializer: KSerializer<T>, tableName: String? = null) =
        UpdateQuery(
            insertQuery(serializer, tableName = tableName)
        ).args<T> {
            nameParams(serializer, it)
        }


    fun <T : Any> decode(serializer: KSerializer<T>, resultSet: ResultSet, columnPrefix: String? = null): T {
        try {
            val decoder =
                SQLDecoder(columnPrefix = columnPrefix, resultSet = resultSet, serializersModule = serializersModule)
            return serializer.deserialize(decoder)
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    @OptIn(InternalSerializationApi::class)
    inline fun <reified T : Any> writer(
        columnPrefix: String? = null,
        vararg overrideFields: Pair<String, Any?>,
    ) =
        writer(serializer = T::class.serializer(), columnPrefix = columnPrefix, overrideFields = overrideFields)

    fun <T : Any> writer(
        serializer: KSerializer<T>,
        columnPrefix: String? = null,
        vararg overrideFields: Pair<String, Any?>,
    ): suspend (T) -> Array<Pair<String, Any?>> = {
        nameParams(
            serializer = serializer,
            value = it,
            columnPrefix = columnPrefix,
            overrideFields = overrideFields,
        )
    }

    @OptIn(InternalSerializationApi::class)
    inline fun <reified T : Any> mapper(columnPrefix: String? = null) =
        mapper(
            serializer = T::class.serializer(),
            columnPrefix = columnPrefix,
        )

    fun <T : Any> mapper(
        serializer: KSerializer<T>,
        columnPrefix: String? = null
    ): suspend (pw.binom.db.async.AsyncResultSet) -> T =
        {
            decode(serializer = serializer, resultSet = it, columnPrefix = columnPrefix)
        }

    @OptIn(InternalSerializationApi::class)
    inline fun <reified T : Any> decode(resultSet: ResultSet, columnPrefix: String? = null): T =
        decode(
            serializer = T::class.serializer(),
            resultSet = resultSet,
            columnPrefix = columnPrefix
        )

    fun <T : Any> buildSqlNamedParams(
        serializer: KSerializer<T>,
        value: T,
        columnPrefix: String? = null,
    ): HashMap<String, Any?> {
        val map = HashMap<String, Any?>()
        buildSqlNamedParamsTo(
            serializer = serializer,
            value = value,
            map = map,
            columnPrefix = columnPrefix,
        )
        return map
    }

    fun <T : Any> nameParams(
        serializer: KSerializer<T>,
        value: T,
        vararg overrideFields: Pair<String, Any?>,
        columnPrefix: String? = null,
    ): Array<Pair<String, Any?>> {
        val result = buildSqlNamedParams(serializer = serializer, value = value, columnPrefix = columnPrefix)
        val it = result.iterator()
        overrideFields.forEach {
            result[it.first] = it.second
        }
        return Array(result.size) { _ ->
            val e = it.next()
            e.key to e.value
        }
    }

    fun <T : Any> buildSqlNamedParamsTo(
        serializer: KSerializer<T>,
        value: T,
        map: MutableMap<String, Any?>,
        columnPrefix: String? = null,
    ) {

        val encoder = SQLEncoder(
            columnPrefix = columnPrefix,
            map = map,
            serializersModule = serializersModule,
        )
        serializer.serialize(encoder, value)
    }
}