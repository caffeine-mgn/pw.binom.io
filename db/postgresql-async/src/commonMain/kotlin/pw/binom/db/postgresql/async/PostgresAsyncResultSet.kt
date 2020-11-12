package pw.binom.db.postgresql.async

import pw.binom.UUID
import pw.binom.date.Date
import pw.binom.date.of
import pw.binom.db.AsyncResultSet
import pw.binom.db.SQLException
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
            ColumnTypes.UUID -> UUID.Companion.create(value).toString()
            else -> throwNotSupported(dataType, value)
        }
    }

    private fun throwNotSupported(dataType: Int, value: ByteArray): Nothing {
        val data = value
            .map { it.toUByte().toString(16).let { if (it.length == 1) "0$it" else it } }
            .joinToString(" ")
        throw SQLException("Unknown Column Type. id: [$dataType], size: [${value.size}], data: [${data}]")
    }

    private fun getIndex(column: String): Int {
        val p = columns.indexOfFirst { it.toLowerCase() == column.toLowerCase() }
        if (p == -1) {
            throw IllegalStateException("Column \"$column\" not found")
        }
        return p
    }

    override fun getDate(index: Int): Date? {
        val value = data[index] ?: return null
        return when (val dataType = data.meta[index].dataType) {
            ColumnTypes.Timestamp -> {
                Long.fromBytes(value).toDatetime()
            }
            ColumnTypes.TimestampWithTimezone -> {
                Long.fromBytes(value).toDatetime()
            }
            else -> {
                throw SQLException("Unknown Column Type. id: [$dataType], size: [${value.size}], data: [${data}]")
            }
        }
    }

    private fun Long.toDatetime() =
        Date(this / 1000).calendar(0).let {
            Date.of(
                year = it.year + 30,
                month = it.month + 0,
                dayOfMonth = it.dayOfMonth,
                hours = it.hours,
                minutes = it.minutes,
                seconds = it.seconds,
                millis = it.millisecond,
                timeZoneOffset = 0
            )
        }

    override fun getDate(column: String): Date? =
        getDate(getIndex(column))

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

    override fun getDouble(index: Int): Double? {
        val value = data[index] ?: return null
        return when (val dataType = data.meta[index].dataType) {
            ColumnTypes.Bigserial -> Long.fromBytes(value).toDouble()
            ColumnTypes.Boolean -> if ((value[0] > 0.toByte())) 1.0 else 0.0
            ColumnTypes.Double -> Double.fromBits(Long.fromBytes(value))
            ColumnTypes.Real -> Float.fromBits(Int.fromBytes(value)).toDouble()
            else -> throwNotSupported(dataType, value)
        }
    }

    override fun getDouble(column: String): Double? =
        getDouble(getIndex(column))

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