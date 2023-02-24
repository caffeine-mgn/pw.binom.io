package pw.binom.pool

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class GenericObjectPoolTest {
    object BoolFactory : ObjectFactory<Boolean> {
        override fun allocate(pool: ObjectPool<Boolean>): Boolean = true

        override fun deallocate(value: Boolean, pool: ObjectPool<Boolean>) {
            // Do nothing
        }
    }

    @Test
    fun shrinkToEmptyTest1() {
        val factory = GenericObjectPool(
            BoolFactory,
            initCapacity = 10,
            growFactor = 1.7f,
            shrinkFactor = 0.5f,
            delayBeforeResize = 0.seconds,
        )
        factory.recycle(factory.borrow())
        factory.borrow()
        assertEquals(0, factory.capacity)
    }

    @Test
    fun shrinkToEmptyTest2() {
        val factory = GenericObjectPool(
            BoolFactory,
            initCapacity = 10,
            growFactor = 1.7f,
            shrinkFactor = 0.5f,
            delayBeforeResize = 0.seconds,
        )
        assertEquals(10, factory.capacity)
        factory.checkTrim()
        assertEquals(0, factory.capacity)
    }

    @Test
    @Ignore
    fun shrinkTest() {
        val factory = GenericObjectPool(BoolFactory, initCapacity = 10, growFactor = 1.7f, shrinkFactor = 0.5f)
        repeat(factory.capacity) {
            factory.recycle(false)
        }
        repeat(4) {
            factory.borrow()
        }
        assertEquals(10, factory.capacity)
        factory.borrow()
        assertEquals(5, factory.capacity)
    }

    @Test
    @Ignore
    fun growTest() {
        val factory = GenericObjectPool(BoolFactory, initCapacity = 10, growFactor = 1.7f, shrinkFactor = 0.5f)
        repeat(factory.capacity) {
            factory.recycle(false)
        }
        assertEquals(10, factory.size)
        assertEquals(10, factory.capacity)
        assertEquals(10, factory.nextCapacity)
        repeat(5) {
            factory.recycle(false)
        }
        assertEquals(10, factory.size)
        assertEquals(10, factory.capacity)
        assertEquals(15, factory.nextCapacity)
        repeat(2) {
            factory.recycle(false)
        }
        assertEquals(10, factory.size)
        assertEquals(17, factory.nextCapacity)
        assertEquals(17, factory.capacity)
    }
}
