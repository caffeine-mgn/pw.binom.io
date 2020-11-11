package pw.binom.db.postgresql.async

import pw.binom.UUID
import pw.binom.db.AsyncResultSet
import pw.binom.decodeString
import pw.binom.fromBytes

class PostgresAsyncResultSet(binary: Boolean, val data: QueryResponse.Data) : AsyncResultSet {
    override val columns: List<String> by lazy { data.meta.map { it.name } }

    override suspend fun next(): Boolean =
        data.next()

    override fun getString(index: Int): String? {
        val value = data[index] ?: return null
        return when (val dataType = data.meta[index].dataType) {
            ColumnTypes.Bigserial -> Long.fromBytes(value).toString()
            ColumnTypes.Varchar -> value.decodeString(data.connection.reader.charset)
            ColumnTypes.Boolean -> (value[0] > 0.toByte()).toString()
            ColumnTypes.UUID-> UUID.Companion.create(value).toString()
            else -> TODO("UNKNOWN $dataType")
        }
    }

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