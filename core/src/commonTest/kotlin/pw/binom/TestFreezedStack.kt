package pw.binom
//
//import pw.binom.thread.FreezedStack
//import kotlin.test.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//
//class TestFreezedStack {
//    @Test
//    fun testLiFo() {
//        val s = FreezedStack<String>()
//        s.pushFirst("1")
//        s.pushFirst("2")
//        assertEquals(2, s.size)
//        assertEquals("2", s.popFirst())
//        assertEquals("1", s.popFirst())
//        assertEquals(0, s.size)
//        assertTrue(s.isEmpty)
//
//        val q = s.asLiFoQueue()
//        q.push("3")
//        q.push("4")
//        assertEquals("4", q.pop())
//        assertEquals("3", q.pop())
//    }
//
//    @Test
//    fun testFiFo() {
//        val s = FreezedStack<String>()
//        s.pushFirst("1")
//        s.pushFirst("2")
//        assertEquals(2, s.size)
//        assertEquals("1", s.popLast())
//        assertEquals("2", s.popLast())
//        assertEquals(0, s.size)
//        assertTrue(s.isEmpty)
//
//        val q = s.asFiFoQueue()
//        q.push("3")
//        q.push("4")
//        assertEquals("3", q.pop())
//        assertEquals("4", q.pop())
//    }
//}