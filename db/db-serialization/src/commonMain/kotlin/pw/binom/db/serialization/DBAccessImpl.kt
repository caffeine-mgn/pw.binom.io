package pw.binom.db.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
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
    override suspend fun <T : Any> selectFrom(
        from: KSerializer<T>,
        queryCondition: String?,
        vararg args: Pair<String, Any?>
    ): List<T> {
        val sb = StringBuilder()
        sb.append("SELECT ")
        val table = from.tableName
        val descriptor = from.descriptor
        repeat(descriptor.elementsCount) {
            if (it > 0) {
                sb.append(", ")
            }
            sb.append(descriptor.getElementName(it))
        }
        sb.append(" FROM ").append(table)
        if (queryCondition != null) {
            sb.append(" ").append(queryCondition)
        }
        return select(query = sb.toString(), args = *args, result = from)
    }

    override suspend fun deleteFrom(
        from: KSerializer<out Any>,
        queryCondition: String?,
        vararg args: Pair<String, Any?>
    ): Long {
        val sb = StringBuilder("delete from ").append(from.tableName)
        if (queryCondition != null) {
            sb.append(" ").append(queryCondition)
        }
        return update(query = sb.toString(), args = args)
    }

    override suspend fun update(query: String, vararg args: Pair<String, Any?>): Long {
        val realQuery = getSqlQuery(query)
        val statement = con.usePreparedStatement(realQuery.sql)
        return statement.executeUpdate(*realQuery.buildArguments(*args))
    }

    override suspend fun <T : Any> update(serializer: KSerializer<T>, value: T, vararg byColumns: String): Boolean {
        val values = sql.nameParams(serializer, value)
        val id = serializer.getIdColumn()
        val sb = StringBuilder()
        sb.append(
            SQLSerialization.updateQuery(
                serializer = serializer,
                excludes = if (byColumns.isEmpty()) setOf(id) else setOf(*byColumns),
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

    override suspend fun <T : Any> find(serializer: KSerializer<T>, key: Any): T? {
        val id = serializer.getIdColumn()
        return selectFrom(
            from = serializer,
            queryCondition = "where $id=:$id limit 1",
            id to key
        ).firstOrNull()
    }

    override suspend fun delete(serializer: KSerializer<out Any>, id: Any): Boolean {
        val idColumn = serializer.getIdColumn()
        return update("delete from ${serializer.tableName} where $idColumn=:id", "id" to id) > 0L
    }

    override suspend fun <T : Any> insert(serializer: KSerializer<T>, value: T): Boolean {
        val query = SQLSerialization.insertQuery(serializer)
        return update(query = query, args = sql.nameParams(serializer, value)) > 0L
    }
}