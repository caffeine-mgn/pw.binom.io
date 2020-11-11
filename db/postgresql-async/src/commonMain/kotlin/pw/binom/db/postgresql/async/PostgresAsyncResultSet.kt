package pw.binom.db.postgresql.async

import pw.binom.db.AsyncResultSet
import pw.binom.db.postgresql.async.messages.frontend.CloseMessage

class PostgresAsyncResultSet(val data: QueryResponse.Data) : AsyncResultSet {
    override val columns: List<String> by lazy { data.meta.map { it.name } }

    override suspend fun next(): Boolean =
        data.next()

    override fun getString(index: Int): String? =
        data[index]

    private fun getIndex(column: String): Int {
        val p = columns.indexOfFirst { it.toLowerCase() == column.toLowerCase() }
        if (p == -1) {
            throw IllegalStateException("Column \"$column\" not found")
        }
        return p
    }

    override fun getString(column: String): String? =
        getString(getIndex(column))

    override fun getBoolean(index: Int): Boolean? {
        TODO("Not yet implemented")
    }

    override fun getBoolean(column: String): Boolean? {
        TODO("Not yet implemented")
    }

    override fun getInt(index: Int): Int? {
        TODO("Not yet implemented")
    }

    override fun getInt(column: String): Int? {
        TODO("Not yet implemented")
    }

    override fun getLong(index: Int): Long? {
        TODO("Not yet implemented")
    }

    override fun getLong(column: String): Long? {
        TODO("Not yet implemented")
    }

    override fun getFloat(index: Int): Float? {
        TODO("Not yet implemented")
    }

    override fun getFloat(column: String): Float? {
        TODO("Not yet implemented")
    }

    override fun getBlob(index: Int): ByteArray? {
        TODO("Not yet implemented")
    }

    override fun getBlob(column: String): ByteArray? {
        TODO("Not yet implemented")
    }

    override fun isNull(index: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun isNull(column: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun close() {
        data.close()
    }

}