package pw.binom.concurrency

import kotlin.test.Test
import kotlin.test.assertEquals

class ConcurrentQueueTest {

    @Test
    fun pushPopTest() {
        val v = ConcurrentQueue<Int>()
        assertEquals(0, v.size)
        v.push(10)
        assertEquals(1, v.size)
        v.push(11)
        assertEquals(2, v.size)
        v.push(12)
        assertEquals(3, v.size)
        assertEquals(10, v.popOrNull())
        assertEquals(2, v.size)
        assertEquals(11, v.popOrNull())
        assertEquals(1, v.size)
        assertEquals(12, v.popOrNull())
        assertEquals(0, v.size)
    }
}
