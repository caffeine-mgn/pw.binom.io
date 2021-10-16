package pw.binom.db.sqlite

import pw.binom.db.DatabaseEngine
import pw.binom.db.async.DatabaseInfo

object SQLiteSQLDatabaseInfo : DatabaseInfo {
    override val tableNameQuotesStart: String
        get() = "\""
    override val tableNameQuotesEnd: String
        get() = "\""
    override val engine: DatabaseEngine
        get() = DatabaseEngine.SQLITE
}