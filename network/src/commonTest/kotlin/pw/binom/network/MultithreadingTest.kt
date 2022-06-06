package pw.binom.network

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.WorkerPool
import pw.binom.io.use
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime

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
        val flag1 = AtomicBoolean(false)
        val nd = NetworkCoroutineDispatcherImpl()
        val executor = WorkerPool(10)
        val addr = NetworkAddress.Immutable("127.0.0.1", 8765)
        val server = launch {
            nd.bindTcp(addr).use { server ->
                val client = server.accept()
                flag1.setValue(true)
            }
        }
        val client = launch {
            nd.tcpConnect(addr)
            Unit
        }
        server.join()
        assertTrue(flag1.getValue(), "flag1 invalid")
    }
}
