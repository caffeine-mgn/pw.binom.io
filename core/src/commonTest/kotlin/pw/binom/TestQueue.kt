package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestQueue {

    @Test
    fun testLiFo() {
        val q = Queue<String>()
        q.pushFirst("1")
        q.pushFirst("2")
        assertEquals("1", q.popLast())
        assertEquals("2", q.popLast())
        assertTrue(q.isEmpty)
    }

    @Test
    fun testFiFo() {
        val q = Queue<String>()
        q.pushFirst("1")
        q.pushFirst("2")
        assertEquals("2", q.popFirst())
        assertEquals("1", q.popFirst())
        assertTrue(q.isEmpty)
    }
}