package pw.binom.db.sqlite

import pw.binom.date.Date
import pw.binom.db.sync.SyncPreparedStatement
import java.sql.Timestamp
import java.sql.PreparedStatement as JPreparedStatement

class SQLSyncPreparedStatement(override val connection: SQLiteConnector, internal val native: JPreparedStatement) :
    SyncPreparedStatement {
    override fun set(index: Int, value: Float) {
        native.setFloat(index + 1, value)
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
        set(index + 1, if (value) 1 else 0)
    }

    override fun set(index: Int, value: ByteArray) {
        native.setBytes(index + 1, value)
    }

    override fun set(index: Int, value: Date) {
        native.setTimestamp(index + 1, Timestamp(value.time))
    }

    override fun setNull(index: Int) {
        native.setNull(index + 1, java.sql.Types.NULL)
//        native.setObject(index + 1, null)
    }

    override fun executeQuery() =
        SQLiteResultSet(native.executeQuery())

    override fun executeUpdate(): Long =
        native.executeUpdate().toLong()

    override fun close() {
        native.close()
    }

}