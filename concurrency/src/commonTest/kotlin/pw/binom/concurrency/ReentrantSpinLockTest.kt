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
                executed.value = true
            }
        }
        assertTrue(executed.value)
    }

    @Test
    fun recursiveWorkerThreadTest() {
        val w = Worker()
        val lock = ReentrantSpinLock()
        val executed = AtomicBoolean(false)
        w.execute(Unit) {
            lock.synchronize {
                lock.synchronize {
                    executed.value = true
                }
            }
        }
        w.requestTermination().joinAndGetOrThrow()
        assertTrue(executed.value)
    }
}