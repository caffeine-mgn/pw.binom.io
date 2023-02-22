package pw.binom.thread

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class FixedThreadExecutorServiceTest {
    @Test
    fun qTest() {
        val list = ArrayList<Int>()
        val pool = FixedThreadExecutorService(1)
        pool.submit {
            Thread.sleep(500.milliseconds)
            list += 10
        }
        pool.submit {
            Thread.sleep(500.milliseconds)
            list += 20
        }
        pool.submit {
            Thread.sleep(500.milliseconds)
            list += 30
        }
        Thread.sleep(2.5.seconds)
        assertEquals(listOf(10, 20, 30), list)
    }
}
