package pw.binom.network

import kotlinx.coroutines.*
import pw.binom.ByteBuffer
import kotlin.test.Test
import pw.binom.*
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertTrue

class CancellationTest {

    @Test
    fun cancellationAccept() {
        val nd = NetworkCoroutineDispatcherImpl()
        var firstReadCanceled = false
        runBlocking {
            val server =
                nd.bindTcp(NetworkAddress.Immutable(host = "127.0.0.1", port = TcpServerConnection.randomPort()))
            val job = launch {
                try {
                    server.accept()
                } catch (e: CancellationException) {
                    firstReadCanceled = true
                } finally {
                    server.close()
                }
            }
            delay(1_000)
            job.cancel()
        }
        assertTrue(firstReadCanceled)
    }

    @Test
    fun cancellationRead() {
        val nd = NetworkCoroutineDispatcherImpl()
        var firstReadCanceled = false
        runBlocking {
            val addr = NetworkAddress.Immutable(host = "127.0.0.1", port = TcpServerConnection.randomPort())
            val server = nd.bindTcp(addr)
            var serverShouldSendResponse = false
            launch {
                try {
                    val c2 = server.accept()!!
                    while (!serverShouldSendResponse) {
                        delay(100)
                    }
                    ByteBuffer.alloc(10) { buf ->
                        c2.write(buf)
                    }
                } finally {
                    server.close()
                }

            }
            val con = nd.tcpConnect(addr)
            val readJob = launch(nd) {
                ByteBuffer.alloc(10) { buf ->
                    try {
                        con.read(buf)
                    } catch (e: CancellationException) {
                        firstReadCanceled = true
                    }
                }
            }
            delay(1000L)
            readJob.cancel()
            serverShouldSendResponse = true
            val readJob2 = launch(nd) {
                ByteBuffer.alloc(10) { buf ->
                    con.read(buf)
                }
            }
            withTimeout(2000L) {
                readJob2.join()
            }
        }
        assertTrue(firstReadCanceled)
    }


}