package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.CompositeDecoder
import pw.binom.collections.defaultArrayList
import pw.binom.collections.defaultHashMap
import pw.binom.db.DatabaseEngine
import pw.binom.db.SQLException
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.async.DatabaseInfo
import pw.binom.db.async.pool.PooledAsyncConnection
import pw.binom.io.use

internal class DBAccessImpl(
    val context: DBContextImpl,
    val con: PooledAsyncConnection,
    val sql: SQLSerialization,
) : DBAccess {

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

    private suspend fun internalSelect(
        query: String,
        vararg args: Pair<String, Any?>,
    ): AsyncResultSet {
        val realQuery = getSqlQuery(query)
        val statement = con.usePreparedStatement(realQuery.sql)
        return statement.executeQuery(*realQuery.buildArguments(*args))
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Any> select(
        query: String,
        vararg args: Pair<String, Any?>,
        result: KSerializer<T>
    ): List<T> {
        val response = internalSelect(query = query, args = args)
        val mapper = getMapper(result)
        val resultList = defaultArrayList<T>()
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
        val d = context.getDescription(fromSerializer)
        val sb = StringBuilder()
        sb.append("SELECT ")
        sb.append("*")
        sb.append(" FROM ").append(d.getSQL())
        if (queryCondition != null) {
            sb.append(" ").append(queryCondition)
        }
        return select(query = sb.toString(), args = args, result = fromSerializer)
    }

    override suspend fun deleteEntityFrom(
        from: KSerializer<out Any>,
        queryCondition: String?,
        args: Array<out Pair<String, Any?>>,
        tableName: String?,
    ): Long {
        val d = context.getDescription(from)
        val sb = StringBuilder("DELETE FROM ").append(d.getSQL())
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
            it.columns.split(',', '|', ':').map { it.trim() }.filter { it.isNotEmpty() }.forEach { column ->
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
                sb.append(" ON CONFLICT (${indexColumns.joinToString(separator = ",")}) DO UPDATE SET ")
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
        includeColumns: Array<String>,
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
                includes = includeColumns.toSet(),
                tableName = tableName
            )
        )
        sb.append(" WHERE ")
        if (byColumns.isEmpty()) {
            sb.append("$id=:$id")
        } else {
            byColumns.forEachIndexed { index, s ->
                if (index != 0) {
                    sb.append(" AND ")
                }
                val el = serializer.descriptor.getElementIndex(s)
                if (el == CompositeDecoder.UNKNOWN_NAME) {
                    throw IllegalArgumentException("Column \"$s\" not found in ${serializer.descriptor.serialName}")
                }
                serializer.descriptor.getElementAnnotations(el)
                val useQuotes = serializer.descriptor.getElementAnnotations(el).any { it is UseQuotes }
                if (useQuotes) {
                    sb.append("\"")
                }
                sb.append(s)
                if (useQuotes) {
                    sb.append("\"")
                }
                sb.append("=:$s")
            }
        }
        return update(query = sb.toString(), args = values) >= 1L
    }

    override suspend fun <T : Any> find(serializer: KSerializer<T>, key: Any, tableName: String?): T? {
        val idColumn = context.getDescription(serializer).id
            ?: throw IllegalArgumentException("Can't find id column in ${serializer.descriptor.serialName}")
        return selectEntityFrom(
            fromSerializer = serializer,
            queryCondition = "WHERE ${idColumn.getSQL()}=:id LIMIT 1",
            tableName = tableName,
            args = arrayOf("id" to key),
        ).firstOrNull()
    }

    override suspend fun deleteEntityFrom(serializer: KSerializer<out Any>, id: Any, tableName: String?): Boolean {
        val idColumn = context.getDescription(serializer).id
            ?: throw IllegalArgumentException("Can't find id column in ${serializer.descriptor.serialName}")
        return update(
            "DELETE FROM ${tableName ?: serializer.tableName} WHERE ${idColumn.getSQL()}=:id",
            "id" to id
        ) > 0L
    }

    override suspend fun <T : Any> insertEntity(
        tableName: String?,
        serializer: KSerializer<T>,
        value: T,
        autoGeneratedResult: (suspend (Map<String, String?>) -> Unit)?
    ): Boolean {
        val des = this.context.getDescription(serializer)
        val sb = StringBuilder()
//        val columnForInsert =
//            if (autoGeneratedResult == null) des.flatColumns() else des.flatColumnsWithoutAutogenerated()
//        val columnForInsert2 =
//            if (autoGeneratedResult == null) des.flatColumns(withoutQuotes = true) else des.flatColumnsWithoutAutogenerated(
//                withoutQuotes = true
//            )
        val columnForInsert = des.flatColumnsWithoutAutogenerated()
        val columnForInsert2 = des.flatColumnsWithoutAutogenerated(withoutQuotes = true)
        sb.append("insert into ")
            .append(des.fullTableName)
            .append(" (")
            .append(columnForInsert.joinToString(","))
            .append(") values (")
            .append(columnForInsert2.joinToString(",") { ":$it" })
            .append(")")
        if (autoGeneratedResult != null) {
            sb.append(
                when (dbDatabaseInfo.engine) {
                    DatabaseEngine.POSTGRESQL -> " RETURNING "
                    DatabaseEngine.SQLITE -> " RETURNING "
                }
            )
            SQLSerialization.getAutogeneratedColumns(serializer).forEachIndexed { index, columnName ->
                if (index > 0) {
                    sb.append(", ")
                }
                sb.append(columnName)
            }

            internalSelect(
                query = sb.toString(),
                args = sql.nameParams(serializer, value),
            ).use {
                if (it.next()) {
                    val generatedMap = defaultHashMap<String, String?>()
                    it.columns.mapIndexed { index, column ->
                        generatedMap[column] = it.getString(index)
                    }
                    autoGeneratedResult(generatedMap)
                } else {
                    autoGeneratedResult(emptyMap())
                }
            }
            return true
        } else {
            return update(query = sb.toString(), args = sql.nameParams(serializer, value)) > 0L
        }
    }

    override fun tableName(serializer: KSerializer<out Any>): String =
        context.getDescription(serializer).fullTableName

    private fun EntityDescription.getSQL() = fullTableName

    private fun EntityDescription.Column.getSQL() = fullColumnName

    override fun columnName(serializer: KSerializer<out Any>, fieldName: String): String =
        context.getDescription(serializer).columns[fieldName]
            ?.getSQL()
            ?: throw IllegalArgumentException("Can't find column \"$fieldName\" inside ${serializer.descriptor.serialName}")
}
