package pw.binom.db.sync

// import com.ionspin.kotlin.bignum.decimal.BigDecimal
// import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.date.Calendar
import pw.binom.date.Date
import pw.binom.date.DateTime
import pw.binom.db.SQLException
import pw.binom.io.Closeable
import pw.binom.uuid.UUID

interface SyncPreparedStatement : Closeable {
  val connection: SyncConnection

  //    fun set(index: Int, value: BigInteger)
//    fun set(index: Int, value: BigDecimal)
  fun set(
    index: Int,
    value: Double,
  )

  fun set(
    index: Int,
    value: Float,
  )

  fun set(
    index: Int,
    value: Int,
  )

  fun set(
    index: Int,
    value: Short,
  )

  fun set(
    index: Int,
    value: Long,
  )

  fun set(
    index: Int,
    value: String,
  )

  fun set(
    index: Int,
    value: Boolean,
  )

  fun set(
    index: Int,
    value: ByteArray,
  )

  fun set(
    index: Int,
    value: DateTime,
  )

  fun set(
    index: Int,
    value: Date,
  )

  fun setNull(index: Int)

  fun executeQuery(): SyncResultSet

  fun executeUpdate(): Long

  fun set(
    index: Int,
    value: UUID,
  ) {
    val buf = ByteArray(UUID.SIZE_BYTES)
    set(index, value.toByteArray(buf))
  }

  fun setValue(
    index: Int,
    value: Any?,
  ) {
    when (value) {
      null -> setNull(index)
//            is BigInteger -> set(index, value)
//            is BigDecimal -> set(index, value)
      is Double -> set(index, value)
      is Float -> set(index, value)
      is Short -> set(index, value)
      is Int -> set(index, value)
      is Long -> set(index, value)
      is String -> set(index, value)
      is Boolean -> set(index, value)
      is ByteArray -> set(index, value)
      is DateTime -> set(index, value)
      is Calendar -> set(index, value.dateTime)
      is UUID -> set(index, value)
      else -> throw SQLException("Can't set value \"$value\" for argument #$index. Type \"${value::class}\" not supported")
    }
  }

  fun executeQuery(vararg arguments: Any?): SyncResultSet {
    arguments.forEachIndexed { index, value ->
      setValue(index, value)
    }
    return executeQuery()
  }

  fun executeUpdate(vararg arguments: Any?): Long {
    arguments.forEachIndexed { index, value ->
      setValue(index, value)
    }
    return executeUpdate()
  }
}
