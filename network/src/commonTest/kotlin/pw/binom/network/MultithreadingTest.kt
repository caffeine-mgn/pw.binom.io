package pw.binom.network

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.concurrency.*
import pw.binom.io.use
import kotlin.test.*
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.seconds

class MultithreadingTest {

//    @Test
//    fun test2() {
//        val nd = NetworkCoroutineDispatcherImpl()
//        val w = Worker.create()
//        var counter by AtomicInt(0)
//        runBlocking {
//            launch {
//                sleep(1000)
//                counter++
//            }
//            assertEquals(1, counter)
//            counter++
//        }
//        assertEquals(2, counter)
//    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun test() = runTest {
        var flag1 by AtomicBoolean(false)
        val nd = NetworkCoroutineDispatcherImpl()
        val executor = WorkerPool(10)
        val addr = NetworkAddress.Immutable("127.0.0.1", 8765)
        val server = launch {
            nd.bindTcp(addr).use { server ->
                val client = server.accept()
                flag1 = true
            }
        }
        val client = launch {
            nd.tcpConnect(addr)
            Unit
        }
        server.join()
        assertTrue(flag1, "flag1 invalid")
    }
}