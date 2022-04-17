package pw.binom.pool

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultPoolTest {

    class Item(var value: Int)

    @Test
    fun test() {
        var newInstanceCount = 0
        val pool = DefaultPool<Item>(5) { newInstanceCount++; Item(0) }

        assertEquals(0, pool.size)
        assertEquals(0, newInstanceCount)
        assertEquals(0, pool.borrow().value)
        assertEquals(0, pool.size)
        assertEquals(1, newInstanceCount)

        val item = Item(0)
        pool.recycle(item)
        assertEquals(1, pool.size)
        assertEquals(1, newInstanceCount)
        assertEquals(pool.borrow(), item)
        assertEquals(0, pool.size)
        assertEquals(1, newInstanceCount)
        repeat(pool.capacity * 2) {
            pool.recycle(Item(0))
        }
        assertEquals(pool.capacity, pool.size)
        repeat(pool.capacity * 2) {
            pool.borrow()
        }
        assertEquals(0, pool.size)
    }
}
