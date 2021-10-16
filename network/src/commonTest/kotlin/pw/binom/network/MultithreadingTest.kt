package pw.binom.network

import pw.binom.atomic.AtomicBoolean
import pw.binom.atomic.AtomicInt
import pw.binom.concurrency.*
import pw.binom.coroutine.start
import kotlin.test.*
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.seconds

class MultithreadingTest {

    @Test
    fun test2() {
        val nd = NetworkDispatcher()
        val w = Worker.create()
        var counter by AtomicInt(0)
        nd.runSingle {
            w.start {
                sleep(1000)
                counter++
            }
            assertEquals(1, counter)
            counter++
        }
        assertEquals(2, counter)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun test() {
        var flag1 by AtomicBoolean(false)
        var flag2 by AtomicBoolean(false)
        val nd = NetworkDispatcher()
        val executor = WorkerPool(10)
        val addr = NetworkAddress.Immutable("127.0.0.1", 8765)
        val server = nd.startCoroutine {
            val server = nd.bindTcp(addr)
            try {
                val client = server.accept()
                val networkThread = ThreadRef()
                executor.start {
                    assertFalse(networkThread.same)
                    flag1 = true
                    nd.start {
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

        val client = nd.startCoroutine {
            nd.tcpConnect(addr)
            Unit
        }

        val now = TimeSource.Monotonic.markNow()
        while (!server.isDone || !client.isDone) {
            if (now.elapsedNow() > 10.0.seconds) {
                fail("timeout")
            }
            nd.select(1000)
        }

        if (server.isFailure) {
            throw server.exceptionOrNull!!
        }
        if (client.isFailure) {
            throw client.exceptionOrNull!!
        }
        assertTrue(flag1)
        assertTrue(flag2)
    }
}