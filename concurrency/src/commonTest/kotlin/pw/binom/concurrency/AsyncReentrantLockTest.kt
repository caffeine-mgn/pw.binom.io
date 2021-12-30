package pw.binom.concurrency

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import pw.binom.atomic.AtomicBoolean

import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AsyncReentrantLockTest {

    @Test
    fun test() = runTest {
        val lock = AsyncReentrantLock()
        val executed = AtomicBoolean(false)

        lock.synchronize {
            lock.synchronize {
                executed.value = true
            }
        }
        assertTrue(executed.value)
    }
}