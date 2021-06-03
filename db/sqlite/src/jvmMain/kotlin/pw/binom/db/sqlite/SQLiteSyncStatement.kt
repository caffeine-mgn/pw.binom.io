package pw.binom.db.sqlite

import pw.binom.db.sync.SyncStatement

class SQLiteSyncStatement(override val connection: SQLiteConnector) : SyncStatement {
    private val native = connection.native.createStatement()

    override fun executeQuery(query: String) =
        SQLiteResultSet(native.executeQuery(query))

    override fun executeUpdate(query: String) =
        native.executeUpdate(query).toLong()

    override fun close() {
        native.close()
    }
}