package pw.binom.io

import pw.binom.atomic.AtomicBoolean
import pw.binom.pool.ObjectPool

class PooledByteBuffer(val pool: ObjectPool<PooledByteBuffer>, size: Int) : ByteBuffer(size) {
    val inPool = AtomicBoolean(true)

    private fun checkPoolState() {
        if (super.isClosed) {
            throw ClosedException()
        }
        if (inPool.getValue()) {
            throw IllegalStateException("ByteBuffer in pool")
        }
    }

    override val isClosed: Boolean
        get() = !inPool.getValue()

    override fun clear() {
        checkPoolState()
        super.clear()
    }

    override fun close() {
        if (super.isClosed) {
            throw IllegalStateException("ByteBuffer already free")
        }
        if (inPool.getValue()) {
            throw IllegalStateException("ByteBuffer already in pool")
        }
        pool.recycle(this)
    }

    fun freeMemory() {
        super.close()
    }
}
