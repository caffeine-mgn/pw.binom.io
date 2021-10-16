package pw.binom.db.postgresql.async

import pw.binom.db.DatabaseEngine
import pw.binom.db.async.DatabaseInfo

object PostgreSQLDatabaseInfo : DatabaseInfo {
    override val tableNameQuotesStart: String
        get() = "\""
    override val tableNameQuotesEnd: String
        get() = "\""
    override val engine: DatabaseEngine
        get() = DatabaseEngine.POSTGRESQL
}