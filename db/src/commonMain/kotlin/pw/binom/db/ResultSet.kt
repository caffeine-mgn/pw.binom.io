package pw.binom.db

// import com.ionspin.kotlin.bignum.decimal.BigDecimal
import pw.binom.date.Date
import pw.binom.date.DateTime
import pw.binom.io.IOException
import pw.binom.uuid.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface ResultSet {
  val columns: List<String>

  fun columnType(index: Int): ColumnType

  fun columnType(column: String): ColumnType

  fun getString(index: Int): String?

  fun getBoolean(index: Int): Boolean?

  fun getInt(index: Int): Int?

  fun getLong(index: Int): Long?

  fun getFloat(index: Int): Float? = getDouble(index)?.toFloat()

//    fun getBigDecimal(index:Int):BigDecimal?
//    fun getBigDecimal(column:String):BigDecimal?

  fun getDouble(index: Int): Double?

  fun getBlob(index: Int): ByteArray?

  fun isNull(index: Int): Boolean

  fun getUUID(index: Int) = getBlob(index)?.let { UUID.create(it) }
  @OptIn(ExperimentalUuidApi::class)
  fun getUuid(index: Int) = getBlob(index)?.let { Uuid.fromByteArray(it) }

  fun getDateTime(index: Int): DateTime?

  fun getDate(index: Int): Date?

  fun getString(column: String): String?

  fun getBoolean(column: String): Boolean?

  fun getInt(column: String): Int?

  fun getLong(column: String): Long?

  fun getFloat(column: String): Float? = getDouble(column)?.toFloat()

  fun getDouble(column: String): Double?

  fun getBlob(column: String): ByteArray?

  fun isNull(column: String): Boolean

  fun getUUID(column: String) = getBlob(column)?.let { UUID.create(it) }
  @OptIn(ExperimentalUuidApi::class)
  fun getUuid(column: String) = getBlob(column)?.let { Uuid.fromByteArray(it) }

  fun getDateTime(column: String): DateTime?

  fun getDate(column: String): Date?

  fun columnIndex(column: String): Int {
    val p = columns.indexOfFirst { it.lowercase() == column.lowercase() }
    if (p == -1) {
      throw IllegalStateException("Column \"$column\" not found")
    }
    return p
  }

  class InvalidColumnTypeException : IOException()

//    enum class ColumnType {
//        STRING, BOOLEAN, INT, LONG, FLOAT, DOUBLE, UUID
//    }
}
