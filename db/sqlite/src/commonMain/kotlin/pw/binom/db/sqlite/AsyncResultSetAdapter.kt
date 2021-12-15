package pw.binom.db.sqlite

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.withContext
import pw.binom.concurrency.*
import pw.binom.date.Date
import pw.binom.db.async.AsyncResultSet
import pw.binom.db.sync.SyncResultSet

class AsyncResultSetAdapter(val ref: SyncResultSet, val worker: Worker, override val columns: List<String>) :
    AsyncResultSet {
    override suspend fun next(): Boolean {
        val ref = ref
        return withContext(worker) {
            ref.next()
        }
    }

    override fun getString(index: Int): String? =
        worker.execute(ref) {
            it.getString(index)
        }.joinAndGetOrThrow()

    override fun getString(column: String): String? =
        worker.execute(ref) {
            it.getString(column)
        }.joinAndGetOrThrow()

    override fun getBoolean(index: Int): Boolean? =
        worker.execute(ref) {
            it.getBoolean(index)
        }.joinAndGetOrThrow()

    override fun getBoolean(column: String): Boolean? =
        worker.execute(ref) {
            it.getBoolean(column)
        }.joinAndGetOrThrow()

    override fun getInt(index: Int): Int? =
        worker.execute(ref) {
            it.getInt(index)
        }.joinAndGetOrThrow()

    override fun getInt(column: String): Int? =
        worker.execute(ref) {
            it.getInt(column)
        }.joinAndGetOrThrow()

    override fun getLong(index: Int): Long? =
        worker.execute(ref) {
            it.getLong(index)
        }.joinAndGetOrThrow()

    override fun getLong(column: String): Long? =
        worker.execute(ref) {
            it.getLong(column)
        }.joinAndGetOrThrow()

    override fun getBigDecimal(index: Int): BigDecimal? =
        worker.execute(ref) {
            it.getBigDecimal(index)
        }.joinAndGetOrThrow()

    override fun getBigDecimal(column: String): BigDecimal? =
        worker.execute(ref) {
            it.getBigDecimal(column)
        }.joinAndGetOrThrow()

    override fun getDouble(index: Int): Double? =
        worker.execute(ref) {
            it.getDouble(index)
        }.joinAndGetOrThrow()

    override fun getDouble(column: String): Double? =
        worker.execute(ref) {
            it.getDouble(column)
        }.joinAndGetOrThrow()

    override fun getBlob(index: Int): ByteArray? =
        worker.execute(ref) {
            it.getBlob(index)
        }.joinAndGetOrThrow()

    override fun getBlob(column: String): ByteArray? =
        worker.execute(ref) {
            it.getBlob(column)
        }.joinAndGetOrThrow()

    override fun isNull(index: Int): Boolean =
        worker.execute(ref) {
            it.isNull(index)
        }.joinAndGetOrThrow()

    override fun isNull(column: String): Boolean =
        worker.execute(ref) {
            it.isNull(column)
        }.joinAndGetOrThrow()

    override fun getDate(index: Int): Date? =
        worker.execute(ref) {
            it.getDate(index)
        }.joinAndGetOrThrow()

    override fun getDate(column: String): Date? =
        worker.execute(ref) {
            it.getDate(column)
        }.joinAndGetOrThrow()

    override fun columnIndex(column: String): Int =
        worker.execute(ref) {
            it.columnIndex(column)
        }.joinAndGetOrThrow()

    override suspend fun asyncClose() {
        val ref = ref
        worker.execute {
            ref.close()
        }
        ref.close()
    }
}