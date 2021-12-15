package pw.binom.concurrency

import pw.binom.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class SpinLockTest {
    @OptIn(ExperimentalTime::class)
    @Test
    fun test() {
        val l = SpinLock()
        val w1 = Worker()
        val w2 = Worker()
        val startFlag = AtomicBoolean(false)

        w1.execute {
            l.synchronize {
                startFlag.value = true
                sleep(5000)
            }
        }
        while (!startFlag.value) {//wait until previous thread start
            sleep(1)
        }
        val duration = w2.execute {
            measureTime {
                l.synchronize { }
            }
        }.joinAndGetOrThrow()
        w1.requestTermination()
        w2.requestTermination()
        val msg = "duration=$duration"
        assertTrue(duration > Duration.seconds(4.9), msg)
        assertTrue(duration < Duration.seconds(6), msg)
    }
}