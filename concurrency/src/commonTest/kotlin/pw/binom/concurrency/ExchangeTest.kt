package pw.binom.concurrency

import pw.binom.ObjectTree
import pw.binom.atomic.AtomicBoolean
import pw.binom.attach
import pw.binom.doFreeze
import kotlin.native.concurrent.SharedImmutable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private class Data(var value: Int)

@SharedImmutable
private var done1 by AtomicBoolean(false)

@SharedImmutable
private var done2 by AtomicBoolean(false)

@SharedImmutable
private val exchange1 = Exchange<ObjectTree<Data>>()

@SharedImmutable
private val exchange2 = Exchange<ObjectTree<Data>>()

@SharedImmutable
private var exceptionExist by AtomicBoolean(false)

class ExchangeTest {

    @Test
    fun passTest() {
        exchange1.doFreeze()
        exchange2.doFreeze()
        val w1 = Worker()
        val w2 = Worker()

        w1.execute(null) {
            println("w1->1")
            try {
                exchange1.put(ObjectTree { Data(0) }.doFreeze())
                println("w1->2")
                val d = exchange2.get().attach()
                println("w1->3  ${d.value}")
                assertEquals(1, d.value)
                println("w1->4")
            } catch (e: Throwable) {
                e.printStackTrace()
                exceptionExist = true
            } finally {
                done1 = true
            }
        }

        w2.execute(null) {
            println("w2->1")
            try {
                println("w2->2")
                val d = ObjectTree {
                    val d = exchange1.get().attach()
                    d.value++
                    d
                }

                exchange2.put(d)
            } catch (e: Throwable) {
                e.printStackTrace()
                exceptionExist = true
            } finally {
                done2 = true
            }
        }

        while (!done1 || !done2) {
            Worker.sleep(100)
        }
        if (exceptionExist) {
            fail("Exception in Worker1")
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun getDelayTest() {
        val e = Exchange<Int>()
        val now = TimeSource.Monotonic.markNow()
        e.get(Duration.seconds(1))
        val duration = now.elapsedNow().inWholeMilliseconds
        assertTrue(duration >= 1000)
        assertTrue(duration < 1500)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun getDelayTest2() {
        val e = Exchange<Int>()
        val w = Worker()
        w.execute(e) {e->
            Worker.sleep(500)
            e.put(100)
        }
        val now = TimeSource.Monotonic.markNow()
        e.get(Duration.seconds(1))
        val duration = now.elapsedNow().inWholeMilliseconds
        assertTrue(duration >= 500)
        assertTrue(duration < 1000)
    }
}