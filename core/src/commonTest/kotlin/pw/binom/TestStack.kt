package pw.binom

import kotlin.test.*

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

    @Test
    fun testIsEmptyLastFirst() {
        val s = Stack<String>()
        s.pushLast("1")
        s.pushLast("2")

        assertFalse(s.isEmpty)
        assertEquals("1", s.popFirst())
        assertFalse(s.isEmpty)
        assertEquals("2", s.popFirst())
        assertTrue(s.isEmpty)
    }

    @Test
    fun testIsEmptyLastLast() {
        val s = Stack<String>()
        s.pushLast("1")
        s.pushLast("2")

        assertFalse(s.isEmpty)
        assertEquals("2", s.popLast())
        assertFalse(s.isEmpty)
        assertEquals("1", s.popLast())
        assertTrue(s.isEmpty)
    }

    @Test
    fun testIsEmptyFirstFirst() {
        val s = Stack<String>()
        s.pushFirst("1")
        s.pushFirst("2")

        assertFalse(s.isEmpty)
        assertEquals("2", s.popFirst())
        assertFalse(s.isEmpty)
        assertEquals("1", s.popFirst())
        assertTrue(s.isEmpty)
    }
}