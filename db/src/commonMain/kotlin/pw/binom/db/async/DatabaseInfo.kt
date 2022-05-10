package pw.binom.db.async

import pw.binom.db.DatabaseEngine

interface DatabaseInfo {
    val tableNameQuotesStart: String
    val tableNameQuotesEnd: String
    val engine: DatabaseEngine
}
