package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.CompositeDecoder
import pw.binom.db.DatabaseEngine
import pw.binom.db.SQLException
import pw.binom.db.async.DatabaseInfo
import pw.binom.db.async.pool.PooledAsyncConnection
import pw.binom.io.use

internal class DBAccessImpl(
    val context: DBContextImpl,
    val con: PooledAsyncConnection,
    val sql: SQLSerialization,
) :
    DBAccess {

    private fun getSqlQuery(query: String) =
        context.statements.getOrPut(query) {
            SQLQueryNamedArguments.parse(
                startQuote = con.dbInfo.tableNameQuotesStart,
                endQuote = con.dbInfo.tableNameQuotesEnd,
                sql = query
            )
        }

    private fun <T : Any> getMapper(result: KSerializer<T>) =
        context.mappers.getOrPut(result) {
            sql.mapper(result)
        }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> select(
        query: String,
        vararg args: Pair<String, Any?>,
        result: KSerializer<T>
    ): List<T> {
        val realQuery = getSqlQuery(query)
        val statement = con.usePreparedStatement(realQuery.sql)
        val response = statement.executeQuery(*realQuery.buildArguments(*args))
        val mapper = getMapper(result)
        val resultList = ArrayList<T>()
        response.use { resp ->
            while (resp.next()) {
                resultList += mapper(resp) as T
            }
        }
        return resultList
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun <T : Any> selectEntityFrom(
        fromSerializer: KSerializer<T>,
        queryCondition: String?,
        args: Array<out Pair<String, Any?>>,
        tableName: String?,
    ): List<T> {
        val sb = StringBuilder()
        sb.append("SELECT ")
        val table = tableName ?: fromSerializer.tableName
        val descriptor = fromSerializer.descriptor
        repeat(descriptor.elementsCount) {
            if (it > 0) {
                sb.append(", ")
            }
            val useQuotes = descriptor.getElementAnnotations(it).any { it is UseQuotes }
            if (useQuotes) {
                sb.append("\"")
            }
            sb.append(descriptor.getElementName(it))
            if (useQuotes) {
                sb.append("\"")
            }
        }
        sb.append(" FROM ").append(table)
        if (queryCondition != null) {
            sb.append(" ").append(queryCondition)
        }
        return select(query = sb.toString(), args = *args, result = fromSerializer)
    }

    override suspend fun deleteEntityFrom(
        from: KSerializer<out Any>,
        queryCondition: String?,
        args: Array<out Pair<String, Any?>>,
        tableName: String?,
    ): Long {
        val sb = StringBuilder("delete from ").append(tableName ?: from.tableName)
        if (queryCondition != null) {
            sb.append(" ").append(queryCondition)
        }
        return update(query = sb.toString(), args = args)
    }

    override suspend fun <T : Any> upsertEntity(
        serializer: KSerializer<T>,
        value: T,
        excludeUpdate: Set<String>,
        updateOnly: Set<String>
    ) {
        if (excludeUpdate.isEmpty() && updateOnly.isNotEmpty()) {
            throw IllegalArgumentException("You should set only one of arguments: excludeUpdate or updateOnly")
        }
        val sb = StringBuilder()
        sb.append(SQLSerialization.insertQuery(serializer))
        val args = sql.nameParams(serializer, value)
        val descriptor = serializer.descriptor
        val indexColumns = HashSet<String>()
        descriptor.annotations.forEach {
            if (it !is Index || !it.unique) {
                return@forEach
            }
            it.columns.forEach { column ->
                val index = descriptor.getElementIndex(column)
                if (index == CompositeDecoder.UNKNOWN_NAME) {
                    throw SQLException("Column with name \"$column\" not found in ${descriptor.serialName}")
                }
                val useQuotes = descriptor.getElementAnnotations(index).any { it is UseQuotes }
                val columnName = if (useQuotes) {
                    "\"$column\""
                } else {
                    column
                }
                indexColumns += columnName
            }
        }
        (0 until serializer.descriptor.elementsCount).forEach { index ->
            val indexColumn = descriptor.getElementAnnotations(index).any { it is IndexColumn }
            if (!indexColumn) {
                return@forEach
            }
            val useQuotes = descriptor.getElementAnnotations(index).any { it is UseQuotes }
            val name = serializer.descriptor.getElementName(index)
            val columnName = if (useQuotes) {
                "\"$name\""
            } else {
                name
            }
            indexColumns += columnName
        }
        when (con.dbInfo.engine) {
            DatabaseEngine.POSTGRESQL,
            DatabaseEngine.SQLITE -> {
                if (indexColumns.isEmpty()) {
                    throw IllegalArgumentException("Not found any index column in ${serializer.descriptor.serialName}")
                }
                sb.append(" on conflict (${indexColumns.joinToString(separator = ",")}) do update set ")
                var first = true
                repeat(serializer.descriptor.elementsCount) { elNum ->
                    val elName = serializer.descriptor.getElementName(elNum)
                    val annotations = serializer.descriptor.getElementAnnotations(elNum)
                    val useQuotes = annotations.any { it is UseQuotes }
                    val id = annotations.any { it is Id }
                    val autoGenerated = annotations.any { it is AutoGenerated }
                    if (id || autoGenerated) {
                        return@repeat
                    }
                    if (excludeUpdate.isNotEmpty() && elName in excludeUpdate) {
                        return@repeat
                    }
                    if (updateOnly.isNotEmpty() && elName !in updateOnly) {
                        return@repeat
                    }
                    if (!first) {
                        sb.append(",")
                    }
                    if (useQuotes) {
                        sb.append("\"")
                    }

                    sb.append(elName)
                    if (useQuotes) {
                        sb.append("\"")
                    }
                    sb.append("=:").append(elName)
                    first = false
                }
            }
        }

        update(
            query = sb.toString(),
            args = args
        )
    }

    override suspend fun update(query: String, vararg args: Pair<String, Any?>): Long {
        val realQuery = getSqlQuery(query)
        val statement = con.usePreparedStatement(realQuery.sql)
        return try {
            statement.executeUpdate(*realQuery.buildArguments(*args))
        } catch (e: Throwable) {
            throw SQLException("Can't execute query \"$query\"", e)
        }
    }

    override val dbDatabaseInfo: DatabaseInfo
        get() = con.dbInfo

    override suspend fun <T : Any> updateEntity(
        serializer: KSerializer<T>,
        value: T,
        tableName: String?,
        excludeColumns: Array<String>,
        byColumns: Array<String>,
    ): Boolean {
        val values = sql.nameParams(serializer, value)
        val id = serializer.getIdColumn()
        val sb = StringBuilder()
        sb.append(
            SQLSerialization.updateQuery(
                serializer = serializer,
                excludes = (if (byColumns.isEmpty()) setOf(id) else setOf(*byColumns)) + excludeColumns,
                tableName = tableName
            )
        )
        sb.append(" where ")
        if (byColumns.isEmpty()) {
            sb.append("$id=:$id")
        } else {
            byColumns.forEachIndexed { index, s ->
                if (index != 0) {
                    sb.append(" and ")
                }
                sb.append("$s=:$s")
            }
        }
        return update(query = sb.toString(), args = values) >= 1L
    }

    override suspend fun <T : Any> find(serializer: KSerializer<T>, key: Any, tableName: String?): T? {
        val id = serializer.getIdColumn()
        return selectEntityFrom(
            fromSerializer = serializer,
            queryCondition = "where $id=:$id limit 1",
            tableName = tableName,
            args = arrayOf(id to key),
        ).firstOrNull()
    }

    override suspend fun deleteEntityFrom(serializer: KSerializer<out Any>, id: Any, tableName: String?): Boolean {
        val idColumn = serializer.getIdColumn()
        return update("delete from ${tableName ?: serializer.tableName} where $idColumn=:id", "id" to id) > 0L
    }

    override suspend fun <T : Any> insertEntity(tableName: String?, serializer: KSerializer<T>, value: T): Boolean {
        val query = SQLSerialization.insertQuery(serializer, tableName = tableName)
        return update(query = query, args = sql.nameParams(serializer, value)) > 0L
    }
}