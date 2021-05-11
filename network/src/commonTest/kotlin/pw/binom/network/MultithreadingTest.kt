package pw.binom.network

import pw.binom.atomic.AtomicBoolean
import pw.binom.concurrency.ThreadRef
import pw.binom.concurrency.WorkerPool
import pw.binom.concurrency.execute
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.*

class MultithreadingTest {
    @OptIn(ExperimentalTime::class)
    @Test
    fun test() {
        var flag1 by AtomicBoolean(false)
        var flag2 by AtomicBoolean(false)
        val nd = NetworkDispatcher()
        val executor = WorkerPool(10)
        val addr = NetworkAddress.Immutable("127.0.0.1", 8765)
        val server = nd.async(executor) {
            val server = nd.bindTcp(addr)
            try {
                val client = server.accept()
                val networkThread = ThreadRef()
                execute {
                    assertFalse(networkThread.same)
                    flag1 = true
                    network {
                        assertTrue(networkThread.same)
                        flag2 = true
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                server.close()
            }
        }

        val client = nd.async(executor) {
            nd.tcpConnect(addr)
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