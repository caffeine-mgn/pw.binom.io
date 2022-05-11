package pw.binom.concurrency

import pw.binom.atomic.AtomicBoolean
import pw.binom.doFreeze
import kotlin.native.concurrent.SharedImmutable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private class Data(var value: Int)

@SharedImmutable
private val done1 = AtomicBoolean(false)

@SharedImmutable
private val done2 = AtomicBoolean(false)

@SharedImmutable
private val exchange1 = BlockingExchange<Data>()

@SharedImmutable
private val exchange2 = BlockingExchange<Data>()

@SharedImmutable
private val exceptionExist = AtomicBoolean(false)

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
                exchange1.put(Data(0))
                println("w1->2")
                val d = exchange2.get()
                println("w1->3  ${d.value}")
                assertEquals(1, d.value)
                println("w1->4")
            } catch (e: Throwable) {
                e.printStackTrace()
                exceptionExist.setValue(true)
            } finally {
                done1.setValue(true)
            }
        }

        w2.execute(null) {
            println("w2->1")
            try {
                println("w2->2")
                val d = run {
                    val d = exchange1.get()
                    d.value++
                    d
                }

                exchange2.put(d)
            } catch (e: Throwable) {
                e.printStackTrace()
                exceptionExist.setValue(true)
            } finally {
                done2.setValue(true)
            }
        }

        while (!done1.getValue() || !done2.getValue()) {
            sleep(100)
        }
        if (exceptionExist.getValue()) {
            fail("Exception in Worker1")
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun getDelayTest() {
        val e = BlockingExchange<Int>()
        val now = TimeSource.Monotonic.markNow()
        e.get(1.seconds)
        val duration = now.elapsedNow().inWholeMilliseconds
        assertTrue(duration >= 1000)
        assertTrue(duration < 1500)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun getDelayTest2() {
        val e = BlockingExchange<Int>()
        val w = Worker()
        w.execute(e) { e ->
            sleep(500)
            e.put(100)
        }
        val now = TimeSource.Monotonic.markNow()
        e.get(1.seconds)
        val duration = now.elapsedNow().inWholeMilliseconds
        assertTrue(duration >= 500)
        assertTrue(duration < 1000)
    }
}
