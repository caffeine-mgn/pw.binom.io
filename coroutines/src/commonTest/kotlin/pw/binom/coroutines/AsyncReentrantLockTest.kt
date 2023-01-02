package pw.binom.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.SpinLock
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AsyncReentrantLockTest {

    @Test
    fun reentrantTest() = runTest {
        val lock = AsyncReentrantLock()
        val executed = AtomicBoolean(false)

        lock.synchronize {
            lock.synchronize {
                executed.setValue(true)
            }
        }
        assertTrue(executed.getValue())
    }

    @Test
    fun test2() = runTest {
        val lock = AsyncReentrantLock()
        val spinLock = SpinLock()
        spinLock.lock()
        launch {
            lock.synchronize {
                spinLock.lock()
                spinLock.unlock()
            }
        }
        launch {
            lock.synchronize {
                println("lock.isLocked->${lock.isLocked}")
            }
        }
        spinLock.unlock()
    }
}
