package pw.binom.concurrency

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import pw.binom.atomic.AtomicBoolean
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
                executed.value = true
            }
        }
        assertTrue(executed.value)
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
