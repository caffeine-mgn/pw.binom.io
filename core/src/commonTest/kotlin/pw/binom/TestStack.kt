package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestStack {

    @Test
    fun testLiFo() {
        val s = Stack<String>().asLiFoQueue()

        assertTrue(s.isEmpty)
        s.push("1")
        assertEquals("1", s.peek())
        s.push("2")
        assertEquals("2", s.peek())
        s.push("3")
        assertEquals("3", s.peek())

        assertEquals("3", s.peek())
        assertEquals("3", s.peek())
        assertEquals("3", s.pop())

        assertEquals("2", s.peek())
        assertEquals("2", s.peek())
        assertEquals("2", s.pop())

        assertEquals("1", s.peek())
        assertEquals("1", s.peek())
        assertEquals("1", s.pop())

        assertTrue(s.isEmpty)
    }

    @Test
    fun testFiFo() {
        val s = Stack<String>().asFiFoQueue()

        assertTrue(s.isEmpty)
        s.push("1")
        assertEquals("1", s.peek())
        s.push("2")
        assertEquals("1", s.peek())
        s.push("3")
        assertEquals("1", s.peek())

        assertEquals("1", s.peek())
        assertEquals("1", s.peek())
        assertEquals("1", s.pop())

        assertEquals("2", s.peek())
        assertEquals("2", s.peek())
        assertEquals("2", s.pop())

        assertEquals("3", s.peek())
        assertEquals("3", s.peek())
        assertEquals("3", s.pop())

        assertTrue(s.isEmpty)
    }
}