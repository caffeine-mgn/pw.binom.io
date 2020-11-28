package pw.binom.db.sqlite

import pw.binom.db.ResultSet
import pw.binom.db.SyncStatement

class SQLiteSyncStatement(override val connection: SQLiteConnector) : SyncStatement {
    private val native = connection.native.createStatement()

    override fun executeQuery(query: String) =
            SQLiteResultSet(native.executeQuery(query))

    override fun executeUpdate(query: String) {
        native.executeUpdate(query)
    }

    override fun close() {
        native.close()
    }
}