package pw.binom.concurrency

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
class WorkerPoolTest {

    @Test
    fun shutdownTestEmpty() {
        val w = WorkerPool()
        w.startCoroutine {
            sleep(1000)
        }.joinAndGetOrThrow()
        val shutdownTime = measureTime {
            w.shutdown()
        }
        assertTrue(shutdownTime < Duration.milliseconds(100))
    }

    @Test
    fun shutdownTestNotEmpty() {
        val w = WorkerPool()
        w.startCoroutine {
            sleep(1000)
        }
        val shutdownTime = measureTime {
            w.shutdown()
        }
        println("shutdownTime=$shutdownTime")
        assertTrue(shutdownTime > Duration.milliseconds(1000))
        assertTrue(shutdownTime < Duration.milliseconds(1500))
    }
}