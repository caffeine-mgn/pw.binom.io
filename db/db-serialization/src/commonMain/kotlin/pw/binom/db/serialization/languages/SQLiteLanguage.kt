package pw.binom.db.serialization.languages

import pw.binom.db.DatabaseEngine
import pw.binom.db.async.DatabaseInfo
import pw.binom.db.serialization.EntityDescription
import pw.binom.db.serialization.SQLLanguage
import pw.binom.db.serialization.SQLQueryNamedArguments

abstract class BasicLanguage : SQLLanguage {
    override fun select(
        query: String?,
        result: EntityDescription,
    ): SQLQueryNamedArguments {
        val sb = StringBuilder()
        sb.append("SELECT ")
        val table = result.tableName
        var firsrt = true
        result.columns.values.forEach {column ->
            if (!firsrt) {
                sb.append(", ")
            }
            firsrt = false
            if (column.useQuotes) {
                sb.append("\"")
            }
            sb.append(column.columnName)
            if (column.useQuotes) {
                sb.append("\"")
            }
        }
        sb.append(" FROM ").append(table)
        if (query != null) {
            sb.append(" ").append(query)
        }
        return SQLQueryNamedArguments.parse(
            startQuote = SQLiteLanguage.tableNameQuotesStart,
            endQuote = SQLiteLanguage.tableNameQuotesEnd,
            sql = sb.toString()
        )
    }
}

object SQLiteLanguage : BasicLanguage() {
    override val tableNameQuotesStart: String
        get() = TODO("Not yet implemented")

    override val tableNameQuotesEnd: String
        get() = TODO("Not yet implemented")

    override fun isSupport(databaseInfo: DatabaseInfo): Boolean =
        databaseInfo.engine == DatabaseEngine.SQLITE

    override fun update(
        includeColumns: Array<String>,
        excludeColumns: Array<String>,
        byColumns: Array<String>,
        result: EntityDescription
    ) {
        TODO("Not yet implemented")
    }


}