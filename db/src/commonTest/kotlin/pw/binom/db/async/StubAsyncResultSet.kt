package pw.binom.db.async

import pw.binom.date.Date
import pw.binom.date.DateTime
import pw.binom.db.ColumnType

class StubAsyncResultSet(
  override val columns: List<String>,
  data: List<List<Any?>>,
) : AsyncResultSet {
  companion object {
    val EMPTY = StubAsyncResultSet(emptyList(), emptyList())
  }

  private val it = data.iterator()
  private var element: List<Any?>? = null

  override suspend fun next(): Boolean {
    if (!it.hasNext()) {
      return false
    }
    element = it.next()
    return true
  }

  override fun columnType(index: Int): ColumnType {
    TODO("Not yet implemented")
  }

  override fun columnType(column: String): ColumnType {
    TODO("Not yet implemented")
  }

  override fun getString(index: Int): String? = element!![index]?.toString()

  private fun getColumn(column: String): Int {
    val index = columns.indexOf(column)
    if (index == -1) {
      throw IllegalArgumentException("Column \"$column\" not found")
    }
    return index
  }

  override fun getString(column: String): String? = getString(getColumn(column))

  override fun getBoolean(index: Int): Boolean? = element!![index]?.let { it as Boolean }

  override fun getBoolean(column: String): Boolean? = getBoolean(getColumn(column))

  override fun getInt(index: Int): Int? = element!![index]?.let { it as Int }

  override fun getInt(column: String): Int? = getInt(getColumn(column))

  override fun getLong(index: Int): Long? = element!![index]?.let { it as Long }

  override fun getLong(column: String): Long? = getLong(getColumn(column))

  override fun getDouble(index: Int): Double? = element!![index]?.let { it as Double }

  override fun getDouble(column: String): Double? = getDouble(getColumn(column))

  override fun getBlob(index: Int): ByteArray? = element!![index]?.let { it as ByteArray }

  override fun getBlob(column: String): ByteArray? = getBlob(getColumn(column))

  override fun isNull(index: Int): Boolean = element!![index] == null

  override fun isNull(column: String): Boolean = isNull(getColumn(column))

  override fun getDateTime(index: Int): DateTime? = element!![index]?.let { it as DateTime }

  override fun getDateTime(column: String): DateTime? = getDateTime(getColumn(column))

  override fun getDate(index: Int): Date? = element!![index]?.let { it as Date }

  override fun getDate(column: String): Date? = getDate(getColumn(column))

  override suspend fun asyncClose() {
  }
}
