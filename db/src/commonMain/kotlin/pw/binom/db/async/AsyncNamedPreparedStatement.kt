package pw.binom.db.async

import pw.binom.db.SQLQueryNamedArguments
import pw.binom.io.AsyncCloseable

/**
 * Async PreparedStatement. Can be used for pass to sql named arguments
 */
class AsyncNamedPreparedStatement internal constructor(
    val ps: AsyncPreparedStatement,
    val args: SQLQueryNamedArguments
) : AsyncCloseable {
    suspend fun executeQuery(vararg arguments: Pair<String, Any?>): AsyncResultSet =
        ps.executeQuery(args.buildArguments(*arguments))

    suspend fun executeUpdate(vararg arguments: Pair<String, Any?>): Long =
        ps.executeUpdate(args.buildArguments(*arguments))

    override suspend fun asyncClose() {
        ps.asyncClose()
    }
}

/**
 * Creates named AsyncPreparedStatement
 */
suspend fun AsyncConnection.prepareNamedStatement(sql: String): AsyncNamedPreparedStatement {
    val args = SQLQueryNamedArguments.parse(sql)
    val ps = prepareStatement(args.sql)
    return AsyncNamedPreparedStatement(
        ps = ps,
        args = args
    )
}