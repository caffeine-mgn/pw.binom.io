package pw.binom.coroutine

import pw.binom.network.NetworkDispatcher
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class AsyncQueueTest {
    @Test
    fun waitMessageTest() {
        val q = AsyncQueue<Int>()
        val nd = NetworkDispatcher()
        val NUM = Random.nextInt()
        var gotValue: Int? = null
        nd.startCoroutine {
            gotValue = q.pop()
        }
        q.push(NUM)
        assertEquals(NUM, gotValue)
    }

    @Test
    fun popMessageTest() {
        val q = AsyncQueue<Int>()
        val nd = NetworkDispatcher()
        val NUM = Random.nextInt()
        q.push(NUM)
        var gotValue: Int? = null
        nd.startCoroutine {
            gotValue = q.pop()
        }
        assertEquals(NUM, gotValue)
    }
}