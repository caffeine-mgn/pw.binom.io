package pw.binom.network

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.concurrency.*
import pw.binom.coroutine.start
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
    fun test() {
        var flag1 by AtomicBoolean(false)
        var flag2 by AtomicBoolean(false)
        val nd = NetworkCoroutineDispatcherImpl()
        val executor = WorkerPool(10)
        val addr = NetworkAddress.Immutable("127.0.0.1", 8765)
        runBlocking {
            val server = launch {
                val server = nd.bindTcp(addr)
                try {
                    val client = server.accept()
                    val networkThread = ThreadRef()
                    executor.start {
                        assertFalse(networkThread.same)
                        flag1 = true
                        launch {
                            assertTrue(networkThread.same)
                            flag2 = true
                        }
                        Unit
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                } finally {
                    server.close()
                }
            }

            val client = launch {
                nd.tcpConnect(addr)
                Unit
            }
        }
        assertTrue(flag1)
        assertTrue(flag2)
    }
}