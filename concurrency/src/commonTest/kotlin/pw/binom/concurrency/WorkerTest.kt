package pw.binom.concurrency

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkerTest {

    /*
    @Test
    fun asyncTest1() {
        val worker1 = Worker()
        val worker2 = Worker()

        var counter = AtomicInt(0)
        val b = worker1.execute {
            runBlocking {
                counter++
                withContext(worker2) {
                    sleep(1000)
                    counter++
                }
                assertEquals(expected = 2, actual = counter.getValue())
                counter++
            }
        }

//        val b1 = worker1.startCoroutine {
//            counter++
//            withContext(worker2) {
//                sleep(1000)
//                counter++
//            }
//            assertEquals(expected = 2, actual = counter)
//            counter++
//        }
        b.joinAndGetOrThrow()
        assertEquals(expected = 3, actual = counter.getValue())
    }

    @Test
    fun asyncTest2() = runBlocking {
        val worker1 = Worker()
        val worker2 = Worker()

        var counter = AtomicInt(0)
        val b = launch(worker1) {
            counter++
            GlobalScope.launch(worker2) {
                delay(5_000)
                counter++
            }
            assertEquals(expected = 1, actual = counter.getValue())
            counter++
        }
        b.join()
        assertEquals(expected = 2, actual = counter.getValue())
    }
*/
    @Test
    fun test() {
        val w = Worker()
        val r = Random.nextInt()
        val r2 = w.execute(r) {
            r + 1
        }.joinAndGetOrThrow()

        assertEquals(r + 1, r2)
    }

    @Test
    fun test2() {
        val w = Worker()
        var r = AtomicInt(Random.nextInt())
        val r2 = w.execute(Unit) {
            r.getValue() + 1
        }.joinAndGetOrThrow()

        assertEquals(r.getValue() + 1, r2)
    }

    @Test
    fun requestTerminationTest() {
        val w = Worker()
        val closed = AtomicBoolean(false)
        val finished = AtomicBoolean(false)
        w.execute(Unit) {
            while (!closed.getValue()) {
                sleep(1000)
            }
            finished.setValue(true)
        }

        sleep(1000)
        closed.setValue(true)
        w.requestTermination().joinAndGetOrThrow()
        assertTrue(finished.getValue())
    }
}
