package pw.binom.db.serialization.languages

import pw.binom.db.DatabaseEngine
import pw.binom.db.async.DatabaseInfo
import pw.binom.db.serialization.EntityDescription
import pw.binom.db.serialization.SQLLanguage
import pw.binom.db.serialization.SQLQueryNamedArguments

abstract class BasicLanguage : SQLLanguage {

//    fun fullTableName(entity: EntityDescription): String {
//        val sb = StringBuilder()
//        fun addWithQ(name: String) {
//            if (entity.useQuotes) {
//                sb.append(tableNameQuotesStart)
//            }
//            sb.append(name)
//            if (entity.useQuotes) {
//                sb.append(tableNameQuotesEnd)
//            }
//        }
//        if (entity.databaseName != null) {
//            addWithQ(entity.databaseName)
//        }
//        if (entity.schemaName != null) {
//            if (entity.databaseName != null) {
//                sb.append(".")
//            }
//            addWithQ(entity.schemaName)
//        }
//        if (entity.databaseName != null || entity.schemaName != null) {
//            sb.append(".")
//        }
//        addWithQ(entity.tableName)
//        return sb.toString()
//    }

    override fun select(
        tableName: String?,
        query: String?,
        result: EntityDescription,
    ): SQLQueryNamedArguments {
        val sb = StringBuilder()
        sb.append("SELECT ")
        var firsrt = true
        result.columns.values.forEach { column ->
            if (!firsrt) {
                sb.append(", ")
            }
            firsrt = false
            sb.append(column.fullColumnName)
        }
        sb.append(" FROM ")
        sb.append(tableName ?: result.fullTableName)
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
