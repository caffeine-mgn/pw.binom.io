package pw.binom.concurrency

import pw.binom.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertTrue

class ReentrantSpinLockTest {
    @Test
    fun recursiveMainThreadTest() {
        val lock = ReentrantSpinLock()
        val executed = AtomicBoolean(false)

        lock.synchronize {
            lock.synchronize {
                executed.setValue(true)
            }
        }
        assertTrue(executed.getValue())
    }

    @Test
    fun recursiveWorkerThreadTest() {
        val w = Worker()
        val lock = ReentrantSpinLock()
        val executed = AtomicBoolean(false)
        w.execute(Unit) {
            lock.synchronize {
                lock.synchronize {
                    executed.setValue(true)
                }
            }
        }
        w.requestTermination().joinAndGetOrThrow()
        assertTrue(executed.getValue())
    }
}
