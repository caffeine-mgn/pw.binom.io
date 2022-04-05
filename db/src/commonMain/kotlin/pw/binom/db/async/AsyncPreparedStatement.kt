package pw.binom.db.async

// import com.ionspin.kotlin.bignum.decimal.BigDecimal
// import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.UUID
import pw.binom.date.Calendar
import pw.binom.date.Date
import pw.binom.db.SQLException
import pw.binom.io.AsyncCloseable

interface AsyncPreparedStatement : AsyncCloseable {
    val connection: AsyncConnection
//    suspend fun set(index: Int, value: BigInteger)
//    suspend fun set(index: Int, value: BigDecimal)
    suspend fun set(index: Int, value: Double)
    suspend fun set(index: Int, value: Float)
    suspend fun set(index: Int, value: Int)
    suspend fun set(index: Int, value: Long)
    suspend fun set(index: Int, value: String)
    suspend fun set(index: Int, value: Boolean)
    suspend fun set(index: Int, value: ByteArray)
    suspend fun set(index: Int, value: Date)
    suspend fun setNull(index: Int)
    suspend fun setValue(index: Int, value: Any?) {
        when (value) {
            null -> setNull(index)
//            is BigInteger -> set(index, value)
//            is BigDecimal -> set(index, value)
            is Double -> set(index, value)
            is Float -> set(index, value)
            is Int -> set(index, value)
            is Long -> set(index, value)
            is String -> set(index, value)
            is Boolean -> set(index, value)
            is ByteArray -> set(index, value)
            is Date -> set(index, value)
            is Calendar -> set(index, value.date)
            is UUID -> set(index, value)
            else -> throw SQLException("Can't set value \"$value\" for argument #$index. Type \"${value::class}\" not supported")
        }
    }

    suspend fun executeQuery(vararg arguments: Any?): AsyncResultSet {
        arguments.forEachIndexed { index, value ->
            setValue(index, value)
        }
        return executeQuery()
    }

    suspend fun executeUpdate(vararg arguments: Any?): Long {
        arguments.forEachIndexed { index, value ->
            setValue(index, value)
        }
        return executeUpdate()
    }

    suspend fun executeQuery(): AsyncResultSet
    suspend fun executeUpdate(): Long
    suspend fun set(index: Int, value: UUID) {
        val buf = ByteArray(16)
        set(index, value.toByteArray(buf))
    }
}
