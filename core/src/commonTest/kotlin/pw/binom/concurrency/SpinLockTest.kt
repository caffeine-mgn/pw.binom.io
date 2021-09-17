package pw.binom.concurrency

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
        val w1 = Worker.create()
        val w2 = Worker.create()


        w1.execute {
            l.synchronize {
                sleep(5000)
            }
        }
        val vv = w2.execute {
            measureTime {
                l.synchronize { }
            }
        }.joinAndGetOrThrow()
        w1.requestTermination()
        w2.requestTermination()
        assertTrue(vv > Duration.seconds(5))
        assertTrue(vv < Duration.seconds(6))
    }
}