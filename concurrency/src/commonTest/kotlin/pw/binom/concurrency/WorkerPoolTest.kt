package pw.binom.concurrency

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
class WorkerPoolTest {

    @Test
    fun shutdownTestEmpty() = runBlocking {
        val w = WorkerPool()
        w.submit {
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
        val lock = SpinLock()
        lock.lock()
        w.submit {
            lock.synchronize {
                val r = TimeSource.Monotonic.markNow()
                sleep(1000)
                println("Sleep time ${r.elapsedNow()}")
            }
        }
        val shutdownTime = measureTime {
            lock.unlock()
            w.shutdown()
        }
        val msg = "shutdownTime=$shutdownTime"
        assertTrue(shutdownTime > Duration.seconds(1), msg)
        assertTrue(shutdownTime < Duration.seconds(1.5), msg)
    }
}