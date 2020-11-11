package pw.binom.db.sqlite

import pw.binom.db.SyncPreparedStatement
import pw.binom.db.ResultSet
import java.sql.PreparedStatement as JPreparedStatement

class SQLSyncPreparedStatement(override val connection: SQLiteConnector, internal val native: JPreparedStatement) : SyncPreparedStatement {
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

    override fun set(index: Int, value: ByteArray) {
        native.setBytes(index + 1, value)
    }

    override fun setNull(index: Int) {
        native.setObject(index + 1, null)
    }

    override fun executeQuery(): ResultSet =
            SQLiteResultSet(native.executeQuery())

    override fun executeUpdate() {
        native.executeUpdate()
    }

    override fun close() {
        native.close()
    }

}