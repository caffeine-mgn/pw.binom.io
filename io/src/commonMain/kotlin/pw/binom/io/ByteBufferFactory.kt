package pw.binom.io

import pw.binom.pool.ObjectFactory
import pw.binom.pool.ObjectPool

class ByteBufferFactory(val size: Int) : ObjectFactory<PooledByteBuffer> {

    override fun allocate(pool: ObjectPool<PooledByteBuffer>): PooledByteBuffer =
        PooledByteBuffer(size = size, pool = pool)

    private fun checkPool(value: PooledByteBuffer, pool: ObjectPool<PooledByteBuffer>) {
        check(value.pool === pool) { "ByteBuffer allocate for other pool" }
    }

    override fun prepare(value: PooledByteBuffer, pool: ObjectPool<PooledByteBuffer>, owner: Any?) {
        checkPool(value = value, pool = pool)
        if (!value.inPool.compareAndSet(true, false)) {
            throw IllegalStateException("ByteBuffer not in pool")
        }
        super.prepare(value = value, pool = pool, owner = owner)
        value.clear()
        value.owner.setValue(owner)
    }

    override fun reset(value: PooledByteBuffer, pool: ObjectPool<PooledByteBuffer>) {
        checkPool(value = value, pool = pool)
        if (!value.inPool.compareAndSet(false, true)) {
            val e = value.owner.getValue()?.hashCode()
            throw IllegalStateException("ByteBuffer already in borrow. owner=$e buffer=${value.hashCode()}")
        }
        value.owner.setValue(null)
        super.reset(value, pool)
    }

    override fun deallocate(value: PooledByteBuffer, pool: ObjectPool<PooledByteBuffer>) {
        value.freeMemory()
    }
}
