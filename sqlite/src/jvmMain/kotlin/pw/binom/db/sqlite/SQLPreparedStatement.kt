package pw.binom.db.sqlite

import pw.binom.db.PreparedStatement
import pw.binom.db.ResultSet
import java.sql.PreparedStatement as JPreparedStatement

class SQLPreparedStatement(override val connection: SQLiteConnector, internal val native: JPreparedStatement) : PreparedStatement {
    override fun set(index: Int, value: Float) {
        native.setFloat(index, value)
    }

    override fun set(index: Int, value: Int) {
        native.setInt(index + 1, value)
    }

    override fun set(index: Int, value: Long) {
        native.setLong(index + 1, value)
    }

    override fun set(index: Int, value: String) {
        native.setString(index + 1, value)
    }

    override fun set(index: Int, value: Boolean) {
        set(index, if (value) 1 else 0)
    }

    override fun executeQuery(): ResultSet =
            SQLiteResultSet(native.executeQuery())

    override fun executeUpdate(query: String) {
        native.executeUpdate()
    }

    override fun close() {
        native.close()
    }

}