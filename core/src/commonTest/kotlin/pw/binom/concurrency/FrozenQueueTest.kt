package pw.binom.concurrency

import kotlin.test.*

class FrozenQueueTest {
    @Test
    fun test() {
        val q = FrozenQueue<Int>()
        assertEquals(0, q.size)
        try {
            q.pop()
            fail()
        } catch (e: NoSuchElementException) {
            //Do nothing
        }
        assertTrue(q.isEmpty)
        q.push(10)
        assertFalse(q.isEmpty)
        assertEquals(1, q.size)
        q.push(20)
        assertEquals(2, q.size)
        assertEquals(10, q.pop())
        assertFalse(q.isEmpty)
        assertEquals(1, q.size)
        assertEquals(20, q.pop())
        assertTrue(q.isEmpty)
        assertEquals(0, q.size)
    }
}