package pw.binom.db.async

import pw.binom.date.Date
import pw.binom.date.DateTime

class StubAsyncPreparedStatement(override val connection: StubConnection, val sql: String) : AsyncPreparedStatement {
  override suspend fun set(
    index: Int,
    value: Double,
  ) {
  }

  override suspend fun set(
    index: Int,
    value: Float,
  ) {
  }

  override suspend fun set(
    index: Int,
    value: Int,
  ) {
  }

  override suspend fun set(
    index: Int,
    value: Short,
  ) {
  }

  override suspend fun set(
    index: Int,
    value: Long,
  ) {
  }

  override suspend fun set(
    index: Int,
    value: String,
  ) {
  }

  override suspend fun set(
    index: Int,
    value: Boolean,
  ) {
  }

  override suspend fun set(
    index: Int,
    value: ByteArray,
  ) {
  }

  override suspend fun set(
    index: Int,
    value: DateTime,
  ) {
  }

  override suspend fun set(
    index: Int,
    value: Date,
  ) {
  }

  override suspend fun setNull(index: Int) {
  }

  override suspend fun executeQuery(): AsyncResultSet = connection.db.select(sql)

  override suspend fun executeUpdate(): Long = connection.db.update(sql)

  override suspend fun asyncClose() {
  }
}
