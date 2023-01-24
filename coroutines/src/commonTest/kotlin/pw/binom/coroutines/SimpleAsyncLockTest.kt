package pw.binom.coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class SimpleAsyncLockTest {
    @Test
    fun cancelTest() = runTest {
        var cancelled = false
        withContext(Dispatchers.Default) {
            val lock = SimpleAsyncLock()
            lock.lock()
            val job = GlobalScope.launch {
                try {
                    lock.lock()
                    lock.unlock()
                } catch (e: CancellationException) {
                    cancelled = true
                }
            }
            delay(1.seconds)
            assertEquals(1, lock.waiters.size)
            assertFalse(cancelled)
            job.cancelAndJoin()
            assertTrue(cancelled)
            assertTrue(lock.isLocked)
            assertTrue(lock.waiters.isEmpty())
        }
    }

    @Test
    fun unlockTest() = runTest {
        withContext(Dispatchers.Default) {
            val lock = SimpleAsyncLock()
            var unlocked = 0
            var jobNum = 0
            lock.lock()
            val job1 = GlobalScope.launch {
                lock.lock()
                jobNum = 1
                unlocked++
            }
            delay(1.seconds)
            val job2 = GlobalScope.launch {
                lock.lock()
                jobNum = 2
                unlocked++
            }
            delay(1.seconds)
            lock.unlock()
            delay(1.seconds)
            when (jobNum) {
                1 -> job1.join()
                2 -> job2.join()
                else -> TODO()
            }
            assertEquals(1, unlocked)
            assertTrue(lock.isLocked)
            assertEquals(1, lock.waiters.size)
        }
    }
}
