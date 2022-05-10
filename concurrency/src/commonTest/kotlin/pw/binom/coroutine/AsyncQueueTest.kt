package pw.binom.coroutine

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class AsyncQueueTest {
    @Test
    fun waitMessageTest() = runBlocking {
        val q = AsyncQueue<Int>()
        val NUM = Random.nextInt()
        var gotValue: Int? = null
        val job = GlobalScope.launch {
            gotValue = q.pop()
        }
        q.push(NUM)
        job.join()
        assertEquals(NUM, gotValue)
    }

    @Test
    fun popMessageTest() = runBlocking {
        val q = AsyncQueue<Int>()
        val NUM = Random.nextInt()
        q.push(NUM)
        var gotValue: Int? = null
        GlobalScope.launch {
            gotValue = q.pop()
        }.join()
        assertEquals(NUM, gotValue)
    }
}
