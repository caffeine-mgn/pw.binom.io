package pw.binom.db.tarantool

import pw.binom.AsyncInput
import pw.binom.ByteBuffer
import pw.binom.atomic.AtomicInt

internal class AsyncInputWithCounter(val input: AsyncInput) : AsyncInput {
    private val _limit = AtomicInt(0)
    var limit: Int
        get() = _limit.getValue()
        set(value) {
            _limit.setValue(value)
        }
    override val available: Int
        get() = minOf(input.available, limit)

    override suspend fun read(dest: ByteBuffer): Int {
        val oldLimit = dest.limit
        dest.limit = dest.position + minOf(dest.limit - dest.position, limit)
        val l = input.read(dest)
        dest.limit = oldLimit
        this.limit -= l
        return l
    }

    override suspend fun asyncClose() {
        input.asyncClose()
    }
}
