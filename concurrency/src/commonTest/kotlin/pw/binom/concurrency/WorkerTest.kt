package pw.binom.concurrency

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.coroutine.start
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkerTest {

    @Test
    fun asyncTest1() {
        val worker1 = Worker.create()
        val worker2 = Worker.create()

        var counter by AtomicInt(0)
        val b = worker1.startCoroutine {
            counter++
            worker2.start {
                sleep(1000)
                counter++
            }
            assertEquals(expected = 2, actual = counter)
            counter++
        }
        b.joinAndGetOrThrow()
        assertEquals(expected = 3, actual = counter)
    }

    @Test
    fun asyncTest2() {
        val worker1 = Worker.create()
        val worker2 = Worker.create()

        var counter by AtomicInt(0)
        val b = worker1.startCoroutine {
            counter++
            worker2.startCoroutine {
                sleep(1000)
                counter++
            }
            assertEquals(expected = 1, actual = counter)
            counter++
        }
        b.joinAndGetOrThrow()
        assertEquals(expected = 2, actual = counter)
    }

    @Test
    fun test() {
        val w = Worker.create()
        val r = Random.nextInt()
        val r2 = w.execute(r) {
            r + 1
        }.resultOrNull!!

        assertEquals(r + 1, r2)
    }

    @Test
    fun test2() {
        val w = Worker.create()
        var r = AtomicInt(Random.nextInt())
        val r2 = w.execute(Unit) {
            r.value + 1
        }.resultOrNull!!

        assertEquals(r.value + 1, r2)
    }

    @Test
    fun requestTerminationTest() {
        val w = Worker.create()
        val closed = AtomicBoolean(false)
        val finished = AtomicBoolean(false)
        w.execute(Unit) {
            while (!closed.value) {
                sleep(1000)
            }
            finished.value = true
        }

        sleep(1000)
        closed.value = true
        w.requestTermination().joinAndGetOrThrow()
        assertTrue(finished.value)
    }
}