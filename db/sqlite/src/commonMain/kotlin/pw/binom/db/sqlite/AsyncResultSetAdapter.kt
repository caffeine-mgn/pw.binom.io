package pw.binom.db.sqlite

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import pw.binom.concurrency.*
import pw.binom.coroutine.start
import pw.binom.date.Date
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.sync.SyncResultSet

class AsyncResultSetAdapter(val ref: Reference<SyncResultSet>, val worker: Worker, override val columns: List<String>) :
    AsyncResultSet {
    override suspend fun next(): Boolean =
        worker.start {
            ref.value.next()
        }

    override fun getString(index: Int): String? =
        worker.execute(ref) {
            it.value.getString(index)
        }.joinAndGetOrThrow()

    override fun getString(column: String): String? =
        worker.execute(ref) {
            it.value.getString(column)
        }.joinAndGetOrThrow()

    override fun getBoolean(index: Int): Boolean? =
        worker.execute(ref) {
            it.value.getBoolean(index)
        }.joinAndGetOrThrow()

    override fun getBoolean(column: String): Boolean? =
        worker.execute(ref) {
            it.value.getBoolean(column)
        }.joinAndGetOrThrow()

    override fun getInt(index: Int): Int? =
        worker.execute(ref) {
            it.value.getInt(index)
        }.joinAndGetOrThrow()

    override fun getInt(column: String): Int? =
        worker.execute(ref) {
            it.value.getInt(column)
        }.joinAndGetOrThrow()

    override fun getLong(index: Int): Long? =
        worker.execute(ref) {
            it.value.getLong(index)
        }.joinAndGetOrThrow()

    override fun getLong(column: String): Long? =
        worker.execute(ref) {
            it.value.getLong(column)
        }.joinAndGetOrThrow()

    override fun getBigDecimal(index: Int): BigDecimal? =
        worker.execute(ref) {
            it.value.getBigDecimal(index)
        }.joinAndGetOrThrow()

    override fun getBigDecimal(column: String): BigDecimal? =
        worker.execute(ref) {
            it.value.getBigDecimal(column)
        }.joinAndGetOrThrow()

    override fun getDouble(index: Int): Double? =
        worker.execute(ref) {
            it.value.getDouble(index)
        }.joinAndGetOrThrow()

    override fun getDouble(column: String): Double? =
        worker.execute(ref) {
            it.value.getDouble(column)
        }.joinAndGetOrThrow()

    override fun getBlob(index: Int): ByteArray? =
        worker.execute(ref) {
            it.value.getBlob(index)
        }.joinAndGetOrThrow()

    override fun getBlob(column: String): ByteArray? =
        worker.execute(ref) {
            it.value.getBlob(column)
        }.joinAndGetOrThrow()

    override fun isNull(index: Int): Boolean =
        worker.execute(ref) {
            it.value.isNull(index)
        }.joinAndGetOrThrow()

    override fun isNull(column: String): Boolean =
        worker.execute(ref) {
            it.value.isNull(column)
        }.joinAndGetOrThrow()

    override fun getDate(index: Int): Date? =
        worker.execute(ref) {
            it.value.getDate(index)
        }.joinAndGetOrThrow()

    override fun getDate(column: String): Date? =
        worker.execute(ref) {
            it.value.getDate(column)
        }.joinAndGetOrThrow()

    override fun columnIndex(column: String): Int =
        worker.execute(ref) {
            it.value.columnIndex(column)
        }.joinAndGetOrThrow()

    override suspend fun asyncClose() {
        worker.start {
            ref.value.close()
        }
        ref.close()
    }
}