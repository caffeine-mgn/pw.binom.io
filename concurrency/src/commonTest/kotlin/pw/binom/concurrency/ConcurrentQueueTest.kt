package pw.binom.concurrency

import kotlin.test.Test
import kotlin.test.assertEquals

class ConcurrentQueueTest {

    @Test
    fun pushPopTest() {
        val v = ConcurrentQueue<Int>()

        v.push(10)
        assertEquals(10, v.popOrNull())
    }
}
