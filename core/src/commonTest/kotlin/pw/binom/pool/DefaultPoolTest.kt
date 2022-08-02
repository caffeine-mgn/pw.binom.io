package pw.binom.pool

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DefaultPoolTest {

    class Item(var value: Int)

    @Test
    fun emptyPoolTest() {
        var newInstanceCount = 0
        val pool = DefaultPool<Item>(5) { newInstanceCount++; Item(0) }

        assertEquals(0, pool.size)
        assertEquals(0, newInstanceCount)
        assertEquals(0, pool.borrow().value)
        assertEquals(0, pool.size)
        assertEquals(1, newInstanceCount)
    }

    @Test
    fun borrowTest() {
        val pool = DefaultPool<Item>(5) { Item(0) }
        val newObj = pool.borrow()
        pool.recycle(newObj)
        assertEquals(newObj, pool.borrow())
        assertNotEquals(newObj, pool.borrow())
    }

    @Test
    fun test() {
        var newInstanceCount = 0
        val pool = DefaultPool<Item>(5) { newInstanceCount++; Item(0) }
        val item = Item(0)
        pool.recycle(item)
        assertEquals(1, pool.size)
        assertEquals(0, newInstanceCount)
        assertEquals(pool.borrow(), item)
        assertEquals(0, pool.size)
        assertEquals(0, newInstanceCount)
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
