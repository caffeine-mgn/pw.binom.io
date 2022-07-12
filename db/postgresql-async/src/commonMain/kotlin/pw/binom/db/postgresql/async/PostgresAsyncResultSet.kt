package pw.binom.db.postgresql.async

// import com.ionspin.kotlin.bignum.decimal.BigDecimal
import pw.binom.UUID
import pw.binom.date.DateTime
import pw.binom.date.of
import pw.binom.db.SQLException
import pw.binom.db.async.AsyncResultSet

class PostgresAsyncResultSet(
    binary: Boolean,
    val data: QueryResponse.Data,
) : AsyncResultSet {
    override val columns: List<String> by lazy { data.meta.map { it.name } }

    override suspend fun next(): Boolean =
        data.next()

    override fun getString(index: Int): String? {
        val value = data[index] ?: return null
        return data.connection.charsetUtils.decode(value)
//        return when (val dataType = data.meta[index].dataType) {
//            ColumnTypes.Bigserial -> Long.fromBytes(value).toString()
//            ColumnTypes.Text, ColumnTypes.Varchar -> data.connection.charsetUtils.decode(value)
//            ColumnTypes.Boolean -> (value[0] > 0.toByte()).toString()
//            ColumnTypes.UUID -> UUID.Companion.create(value).toString()
//            else -> throwNotSupported(dataType, value)
//        }
    }

    private fun throwNotSupported(dataType: Int, value: ByteArray): Nothing {
        val data = value
            .map { it.toUByte().toString(16).let { if (it.length == 1) "0$it" else it } }
            .joinToString(" ")
        throw SQLException("Unknown Column Type. id: [$dataType], size: [${value.size}], data: [$data]")
    }

    private fun getIndex(column: String): Int {
        val p = columns.indexOfFirst { it.lowercase() == column.lowercase() }
        if (p == -1) {
            throw IllegalStateException("Column \"$column\" not found")
        }
        return p
    }

    override fun getUUID(index: Int): UUID? {
        val uuid = getString(index) ?: return null
        return UUID.fromString(uuid)
    }

    override fun getUUID(column: String): UUID? =
        getUUID(getIndex(column))

    override fun getDate(index: Int): DateTime? {
        val value = getString(index) ?: return null
        return DateUtils.parseDate(value)
    }

    private fun Long.toDatetime() =
        DateTime(this / 1000).calendar(0).let {
            DateTime.of(
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

    override fun getDate(column: String): DateTime? =
        try {
            getDate(getIndex(column))
        } catch (e: Throwable) {
            println("->>>>$column<<<<-")
            throw e
//            throw PostgresqlException("Can't get Date from column \"$column\"", e)
        }

    override fun columnIndex(column: String): Int = getIndex(column)

    override fun getString(column: String): String? =
        getString(getIndex(column))

    override fun getBoolean(index: Int): Boolean? {
        val value = getString(index) ?: return null
        return value == "t" || value == "true"
//        val value = data[index] ?: return null
//        return when (val dataType = data.meta[index].dataType) {
//            ColumnTypes.Bigserial -> Long.fromBytes(value) > 0
//            ColumnTypes.Text, ColumnTypes.Varchar -> data.connection.charsetUtils.decode(value) == "true"
//            ColumnTypes.Boolean -> (value[0] > 0.toByte())
//            else -> throwNotSupported(dataType, value)
//        }
    }

    override fun getBoolean(column: String): Boolean? =
        getBoolean(getIndex(column))

    override fun getInt(index: Int): Int? {
        return getString(index)?.toInt()
//        return when (val dataType = data.meta[index].dataType) {
//            ColumnTypes.Bigserial -> Long.fromBytes(value).toInt()
//            ColumnTypes.Boolean -> if ((value[0] > 0.toByte())) 1 else 0
//            ColumnTypes.Double -> Double.fromBits(Long.fromBytes(value)).toInt()
//            ColumnTypes.Real -> Float.fromBits(Int.fromBytes(value)).toInt()
//            ColumnTypes.Numeric -> NumericUtils.decode(value).intValue()
//            else -> throwNotSupported(dataType, value)
//        }
    }

    override fun getInt(column: String): Int? =
        getInt(getIndex(column))

    override fun getLong(index: Int): Long? {
        return getString(index)?.toLong()
//        val value = data[index] ?: return null
//        return when (val dataType = data.meta[index].dataType) {
//            ColumnTypes.Bigserial -> Long.fromBytes(value)
//            ColumnTypes.Boolean -> if ((value[0] > 0.toByte())) 1L else 0L
//            ColumnTypes.Double -> Double.fromBits(Long.fromBytes(value)).toLong()
//            ColumnTypes.Real -> Float.fromBits(Int.fromBytes(value)).toLong()
//
//            ColumnTypes.Numeric -> NumericUtils.decode(value).longValue()
//            else -> throwNotSupported(dataType, value)
//        }
    }

    override fun getLong(column: String): Long? =
        getLong(getIndex(column))

//    override fun getBigDecimal(index: Int): BigDecimal? {
//        return getString(index)?.let { BigDecimal.parseString(it) }
//    }
//
//    override fun getBigDecimal(column: String): BigDecimal? =
//        getBigDecimal(getIndex(column))

    override fun getDouble(index: Int): Double? {
        return getString(index)?.toDouble()
//        val value = data[index] ?: return null
//        return when (val dataType = data.meta[index].dataType) {
//            ColumnTypes.Bigserial -> Long.fromBytes(value).toDouble()
//            ColumnTypes.Boolean -> if ((value[0] > 0.toByte())) 1.0 else 0.0
//            ColumnTypes.Double -> Double.fromBits(Long.fromBytes(value))
//            ColumnTypes.Real -> Float.fromBits(Int.fromBytes(value)).toDouble()
//            ColumnTypes.Numeric -> NumericUtils.decode(value).toString().toDouble()
//            else -> throwNotSupported(dataType, value)
//        }
    }

    override fun getDouble(column: String): Double? =
        getDouble(getIndex(column))

    override fun getBlob(index: Int): ByteArray? {
        val value = getString(index) ?: return null
        if (value.length <= 2 || value[0] != '\\' || value[1] != 'x' || value.length % 2 != 0) {
            throw IllegalArgumentException("Can't parse \"$value\" to ByteArray")
        }
        return ByteArray((value.length - 2) / 2) {
            val first = value[2 + it * 2 + 0].digitToInt(16)
            val second = value[2 + it * 2 + 1].digitToInt(16)
            ((first shl 4) or (second and 0xF)).toByte()
        }
    }

    override fun getBlob(column: String): ByteArray? =
        getBlob(getIndex(column))

    override fun isNull(index: Int): Boolean =
        data[index] == null

    override fun isNull(column: String): Boolean =
        isNull(getIndex(column))

    var isClosed = false
        private set

    override suspend fun asyncClose() {
        if (!isClosed) {
            isClosed = true
            if (!data.isClosed) {
                data.asyncClose()
            }
        }
    }
}
