package pw.binom.db.postgresql.async

import pw.binom.db.async.DatabaseInfo

object PostgreSQLDatabaseInfo : DatabaseInfo {
    override val tableNameQuotesStart: String
        get() = "\""
    override val tableNameQuotesEnd: String
        get() = "\""
}