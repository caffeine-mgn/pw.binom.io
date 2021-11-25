package pw.binom.db.serialization

import pw.binom.db.async.DatabaseInfo

interface SQLLanguage {
    val tableNameQuotesStart: String
    val tableNameQuotesEnd: String
    fun isSupport(databaseInfo: DatabaseInfo): Boolean

    fun select(
        tableName:String?,
        query: String?,
        result: EntityDescription,
    ): SQLQueryNamedArguments

    fun update(
        includeColumns: Array<String>,
        excludeColumns: Array<String>,
        byColumns: Array<String>,
        result: EntityDescription,
    )
}