package pw.binom.concurrency

import pw.binom.atomic.AtomicInt
import kotlin.test.Test
import kotlin.test.assertEquals

class SpinLockTest {
    @Test
    fun lockTest() {
        val l = SpinLock()
        val atom = AtomicInt(0)
        val errorCount = AtomicInt(0)

        val w1 = Worker()
        val w2 = Worker()

        val b1 = w1.execute(Unit) {
            repeat(10) {
                l.synchronize {
                    if (atom.getValue() == 0) {
                        atom.setValue(1)
                        println("OK-1")
                        sleep(50)
                        atom.setValue(0)
                    } else {
                        println("ERROR-1")
                        errorCount.increment()
                    }
                }
            }
        }

        val b2 = w2.execute(Unit) {
            repeat(10) {
                l.synchronize {
                    if (atom.getValue() == 0) {
                        atom.setValue(1)
                        println("OK-2")
                        sleep(100)
                        atom.setValue(0)
                    } else {
                        println("ERROR-2")
                        errorCount.increment()
                    }
                }
            }
        }

        sleep(500)
        while (!b1.isDone || !b2.isDone) {
            sleep(100)
        }
        assertEquals(0, errorCount.getValue())
    }
}
