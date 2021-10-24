package pw.binom.db.serialization.languages

import pw.binom.db.async.DatabaseInfo
import pw.binom.db.serialization.EntityDescription
import pw.binom.db.serialization.SQLLanguage

object PostgreSQLLanguage: BasicLanguage() {
    override val tableNameQuotesStart: String
        get() = TODO("Not yet implemented")
    override val tableNameQuotesEnd: String
        get() = TODO("Not yet implemented")

    override fun isSupport(databaseInfo: DatabaseInfo): Boolean {
        TODO("Not yet implemented")
    }

    override fun update(
        includeColumns: Array<String>,
        excludeColumns: Array<String>,
        byColumns: Array<String>,
        result: EntityDescription
    ) {
        TODO("Not yet implemented")
    }
}