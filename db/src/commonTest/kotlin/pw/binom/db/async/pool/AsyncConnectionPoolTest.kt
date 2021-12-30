package pw.binom.db.async.pool

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import pw.binom.db.async.StubConnection
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
class AsyncConnectionPoolTest {

    suspend fun AsyncConnectionPool.sleepAndUnlock(m: Mutex, time: Long) {
        borrow {
            m.unlock()
            usePreparedStatement("sleep $time").executeUpdate()
        }
    }

    suspend fun AsyncConnectionPool.ping() {
        borrow {
            usePreparedStatement("select 1").executeQuery().asyncClose()
        }
    }

    fun runTest2(
        testBody: suspend CoroutineScope.() -> Unit
    ) = runTest {
        withContext(Dispatchers.Default) {
            testBody(this)
        }
    }

    @Test
    fun maxTest() = runTest2 {
        withContext(Dispatchers.Default) {
            val pool = AsyncConnectionPool.create(
                maxConnections = 1,
            ) {
                StubConnection()
            }
            val m = Mutex()
            m.lock()
            val busyTime = async {
                measureTime {
                    pool.sleepAndUnlock(m, 1000)
                }
            }
            m.lock()
            val getNewConnectionTime = measureTime {
                pool.ping()
            }
            println("busyTime=${busyTime.await()}")
            println("getNewConnectionTime=$getNewConnectionTime")
            assertTrue(busyTime.await() > 1.0.seconds && busyTime.await() < 1.5.seconds, "busyTime failed")
            assertTrue(
                getNewConnectionTime > 1.0.seconds && getNewConnectionTime < 1.5.seconds,
                "getNewConnectionTime failed"
            )
        }
    }
}
