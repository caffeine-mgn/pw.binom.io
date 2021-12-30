package pw.binom.db.async

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.coroutines.delay
import pw.binom.date.Date

class StubAsyncPreparedStatement(override val connection: StubConnection, val sql: String) : AsyncPreparedStatement {
    override suspend fun set(index: Int, value: BigInteger) {
    }

    override suspend fun set(index: Int, value: BigDecimal) {
    }

    override suspend fun set(index: Int, value: Double) {
    }

    override suspend fun set(index: Int, value: Float) {
    }

    override suspend fun set(index: Int, value: Int) {
    }

    override suspend fun set(index: Int, value: Long) {
    }

    override suspend fun set(index: Int, value: String) {
    }

    override suspend fun set(index: Int, value: Boolean) {
    }

    override suspend fun set(index: Int, value: ByteArray) {
    }

    override suspend fun set(index: Int, value: Date) {
    }

    override suspend fun setNull(index: Int) {
    }

    override suspend fun executeQuery(): AsyncResultSet = connection.db.select(sql)
    override suspend fun executeUpdate(): Long = connection.db.update(sql)

    override suspend fun asyncClose() {
    }
}