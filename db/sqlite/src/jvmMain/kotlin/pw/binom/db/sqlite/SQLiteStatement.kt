package pw.binom.db.sqlite

import pw.binom.db.ResultSet
import pw.binom.db.Statement

class SQLiteStatement(override val connection: SQLiteConnector) : Statement {
    private val native = connection.native.createStatement()

    override fun executeQuery(query: String): ResultSet =
            SQLiteResultSet(native.executeQuery(query))

    override fun executeUpdate(query: String) {
        native.executeUpdate(query)
    }

    override fun close() {
        native.close()
    }
}