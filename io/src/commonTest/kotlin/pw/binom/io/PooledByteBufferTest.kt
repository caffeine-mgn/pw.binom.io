package pw.binom.io

import pw.binom.pool.ObjectFactory
import pw.binom.pool.ObjectPool
import pw.binom.pool.using
import kotlin.test.Test
import kotlin.test.assertEquals

class PooledByteBufferTest {

    class MyPool(val factory: ObjectFactory<PooledByteBuffer>) : ObjectPool<PooledByteBuffer> {
        var created = 0
            private set
        var closed = 0
            private set

        override fun borrow(owner: Any?): PooledByteBuffer {
            val b = factory.allocate(this)
            factory.prepare(value = b, pool = this, owner = owner)
            created++
            return b
        }

        override fun recycle(value: PooledByteBuffer) {
            factory.reset(value, this)
            factory.deallocate(value, this)
            closed++
        }

        override fun close() {
        }
    }

    @Test
    fun closeTest() {
        val pool = MyPool(ByteBufferFactory(100))
        val buffer = pool.borrow()
        assertEquals(1L, ByteBufferMetric.BYTEBUFFER_COUNT_METRIC.value)
        assertEquals(1, pool.created)
        assertEquals(0, pool.closed)
        buffer.close()
        assertEquals(1, pool.created)
        assertEquals(1, pool.closed)
        assertEquals(0L, ByteBufferMetric.BYTEBUFFER_COUNT_METRIC.value)
    }

    @Test
    fun usingTest() {
        val pool = MyPool(ByteBufferFactory(100))
        pool.using {
            assertEquals(1L, ByteBufferMetric.BYTEBUFFER_COUNT_METRIC.value)
            assertEquals(1, pool.created)
            assertEquals(0, pool.closed)
        }
        assertEquals(1, pool.created)
        assertEquals(1, pool.closed)
        assertEquals(0L, ByteBufferMetric.BYTEBUFFER_COUNT_METRIC.value)
    }
}
