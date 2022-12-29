package pw.binom.io

import pw.binom.atomic.AtomicBoolean
import pw.binom.pool.ObjectPool

class PooledByteBuffer(val pool: ObjectPool<PooledByteBuffer>, size: Int) : ByteBuffer(size) {
    val inPool = AtomicBoolean(true)

    private fun checkPoolState() {
        if (inPool.getValue()) {
            throw IllegalStateException("ByteBuffer in pool")
        }
    }

    override fun clear() {
        checkPoolState()
        super.clear()
    }

    override fun close() {
        pool.recycle(this)
    }

    fun freeMemory() {
        super.close()
    }
}
