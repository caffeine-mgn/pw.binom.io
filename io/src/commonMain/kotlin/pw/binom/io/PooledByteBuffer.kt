package pw.binom.io

import pw.binom.atomic.AtomicBoolean
import pw.binom.pool.ObjectPool

class PooledByteBuffer(val pool: ObjectPool<PooledByteBuffer>, size: Int) : ByteBuffer(size) {
    val inPool = AtomicBoolean(true)
    private val deallocation = AtomicBoolean(false)

    private fun checkPoolState() {
        if (super.isClosed) {
            throw ClosedException()
        }
        if (inPool.getValue()) {
            throw IllegalStateException("ByteBuffer in pool")
        }
    }

    private fun ensureOpenNotInPool() {
        if (deallocation.getValue()) {
            return
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
            throw IllegalStateException("Can't reuse ByteBuffer: ByteBuffer already free")
        }
        ensureOpenNotInPool()
        pool.recycle(this)
    }

    fun freeMemory() {
        if (!inPool.getValue()) {
            throw IllegalStateException("Cannot deallocate ByteBuffer: ByteBuffer not in pool")
        }
        deallocation.setValue(true)
        super.close()
    }

    override fun ensureOpen() {
        ensureOpenNotInPool()
        super.ensureOpen()
    }
}
