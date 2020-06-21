package pw.binom.thread

import pw.binom.Stack
import kotlin.test.*
import kotlin.time.*

@OptIn(ExperimentalTime::class)
class SynchronizedAppendableQueueTest {

    @Ignore
    @Test
    fun `Pop with Duration`() {
        val q = Stack<Int>().asFiFoQueue().synchronized()

        var executeTime = Duration.ZERO

        val thread = Thread(Runnable {
            executeTime = measureTime {
                val t = q.pop(5.0.toDuration(DurationUnit.SECONDS))
                assertNull(t)
            }

            println("Time: $executeTime")
        })
        thread.start()
        thread.join()

        assertTrue(executeTime >= 4900.0.toDuration(DurationUnit.MILLISECONDS) && executeTime <= 5500.0.toDuration(DurationUnit.MILLISECONDS))
    }

    @Ignore
    @Test
    fun `Pop with Duration 2`() {
        val q = Stack<Int>().asFiFoQueue().synchronized()

        var executeTime = Duration.ZERO

        val thread = Thread(Runnable {
            executeTime = measureTime {
                val t = q.pop(5.0.toDuration(DurationUnit.SECONDS))
                assertEquals(10, t)
            }

            println("Time: $executeTime")
        })
        thread.start()
        Thread.sleep(1000)
        q.push(10)
        thread.join()

        assertTrue(executeTime >= 950.0.toDuration(DurationUnit.MILLISECONDS) && executeTime <= 1500.0.toDuration(DurationUnit.MILLISECONDS))
    }

    @Test
    fun `Use in thread pool`(){
        val pool = FixedThreadPool(10)
        Thread.sleep(1000)
        pool.close()
    }
}