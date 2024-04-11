package pw.binom.db.sqlite

import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import pw.binom.date.Date
import pw.binom.date.DateTime
import pw.binom.db.ColumnType
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.sync.SyncResultSet
import kotlin.coroutines.CoroutineContext

class AsyncResultSetAdapter(val ref: SyncResultSet, val context: CoroutineContext, override val columns: List<String>) :
  AsyncResultSet {
  override suspend fun next(): Boolean {
    val ref = ref
    return withContext(context) {
      withTimeout(ASYNC_TIMEOUT) {
        ref.next()
      }
    }
  }

  override fun columnType(index: Int): ColumnType = ref.columnType(index)

  override fun columnType(column: String): ColumnType = ref.columnType(column)

  override fun getString(index: Int): String? = ref.getString(index)

  override fun getString(column: String): String? = ref.getString(column)

  override fun getBoolean(index: Int): Boolean? = ref.getBoolean(index)

  override fun getBoolean(column: String): Boolean? = ref.getBoolean(column)

  override fun getInt(index: Int): Int? = ref.getInt(index)

  override fun getInt(column: String): Int? = ref.getInt(column)

  override fun getLong(index: Int): Long? = ref.getLong(index)

  override fun getLong(column: String): Long? = ref.getLong(column)

//    override fun getBigDecimal(index: Int): BigDecimal? =
//        worker.execute(ref) {
//            it.getBigDecimal(index)
//        }.joinAndGetOrThrow()
//
//    override fun getBigDecimal(column: String): BigDecimal? =
//        worker.execute(ref) {
//            it.getBigDecimal(column)
//        }.joinAndGetOrThrow()

  override fun getDouble(index: Int): Double? = ref.getDouble(index)

  override fun getDouble(column: String): Double? = ref.getDouble(column)

  override fun getBlob(index: Int): ByteArray? = ref.getBlob(index)

  override fun getBlob(column: String): ByteArray? = ref.getBlob(column)

  override fun isNull(index: Int): Boolean = ref.isNull(index)

  override fun isNull(column: String): Boolean = ref.isNull(column)

  override fun getDateTime(index: Int): DateTime? = ref.getDateTime(index)

  override fun getDateTime(column: String): DateTime? = ref.getDateTime(column)

  override fun getDate(index: Int): Date? = ref.getDate(index)

  override fun getDate(column: String): Date? = ref.getDate(column)

  override fun columnIndex(column: String): Int = ref.columnIndex(column)

  override suspend fun asyncClose() {
    val ref = ref
    withContext(context) {
      withTimeout(ASYNC_TIMEOUT) {
        ref.close()
      }
    }
  }
}
