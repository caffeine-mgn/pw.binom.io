package pw.binom.concurrency

import pw.binom.atomic.AtomicReference
import pw.binom.getOrException
import pw.binom.network.NetworkDispatcher
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class DeadlineTimerTest {

    @Test
    fun addAllAndWait() {
        val deadlineTimer = DeadlineTimerImpl()
        val now = TimeSource.Monotonic.markNow()
        var f1 by AtomicReference<Duration?>(null)
        var f2 by AtomicReference<Duration?>(null)
        var f3 by AtomicReference<Duration?>(null)
        var f4 by AtomicReference<Duration?>(null)
        deadlineTimer.delay(Duration.seconds(1.0)) {
            f1 = now.elapsedNow()
        }
        deadlineTimer.delay(Duration.seconds(2)) {
            f2 = now.elapsedNow()
        }
        deadlineTimer.delay(Duration.seconds(3)) {
            f3 = now.elapsedNow()
        }
        deadlineTimer.delay(Duration.seconds(6)) {
            f4 = now.elapsedNow()
        }
        sleep(4000)
        assertNotNull(f1)
        assertNotNull(f2)
        assertNotNull(f3)
        assertNull(f4)

        assertTrue(f1!! >= Duration.seconds(1) && f1!! < Duration.seconds(1.1),"f1=$f1")
        assertTrue(f2!! >= Duration.seconds(2) && f2!! < Duration.seconds(2.1),"f2=$f2")
        assertTrue(f3!! >= Duration.seconds(3) && f2!! < Duration.seconds(3.1), "f3=$f3")
    }

    @Test
    fun addInWait() {
        val deadlineTimer = DeadlineTimerImpl()
        var f1 by AtomicReference<Duration?>(null)
        var f2 by AtomicReference<Duration?>(null)
        val now = TimeSource.Monotonic.markNow()
        deadlineTimer.delay(Duration.seconds(2)) {
            f1 = now.elapsedNow()
        }

        sleep(1000)
        deadlineTimer.delay(Duration.seconds(1)) {
            f2 = now.elapsedNow()
        }
        sleep(2000)
        assertNotNull(f1)
        assertNotNull(f2)
        assertTrue(f1!! >= Duration.seconds(2) && f1!! < Duration.seconds(2.2),"f1=$f1")
        assertTrue(f2!! >= Duration.seconds(2) && f2!! < Duration.seconds(2.2),"f2=$f2")
    }

    @Test
    fun asyncDelayTest() {
        val nd = NetworkDispatcher()
        val deadlineTimer = DeadlineTimerImpl()
        val future = nd.startCoroutine {
            val now = TimeSource.Monotonic.markNow()
            val currentThread = ThreadRef()
            deadlineTimer.delay(Duration.seconds(1))
            val f1 = now.elapsedNow()
            assertTrue(f1 >= Duration.seconds(1) && f1 < Duration.seconds(1.1))
            assertTrue(currentThread.same)
        }
        while (!future.isDone) {
            nd.select(1000)
        }
        future.getOrException()
    }

    @Test
    fun delayTest() {
        val deadlineTimer = DeadlineTimerImpl()
        val nd = NetworkDispatcher()

        val d1 = nd.startCoroutine {
            deadlineTimer.delay(Duration.seconds(1.1))
        }
        val d2 = nd.startCoroutine {
            deadlineTimer.delay(Duration.seconds(1.2))
        }
        val d3 = nd.startCoroutine {
            deadlineTimer.delay(Duration.seconds(1))
        }

        while (!d1.isDone || !d2.isDone || !d3.isDone) {
//            println("d1=${d1.isDone}, d2=${d2.isDone}, d3=${d3.isDone}")
            nd.select(100)
        }
        d1.joinAndGetOrThrow()
        d2.joinAndGetOrThrow()
        d3.joinAndGetOrThrow()
        deadlineTimer.close()
    }
}