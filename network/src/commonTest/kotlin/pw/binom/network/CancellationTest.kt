package pw.binom.network

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import pw.binom.io.ByteBuffer
import pw.binom.io.use
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CancellationTest {

    @Test
    fun cancelNetworkManager() = runTest {
        withContext(Dispatchers.Network) {
            val backgroundJob = CoroutineScope(Dispatchers.Network).launch {
                try {
                    delay(1.minutes)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    throw e
                }
            }
            backgroundJob.cancelAndJoin()
        }
    }

    @Test
    fun cancellationAccept() = runTest(dispatchTimeoutMs = 5_000) {
        withContext(Dispatchers.Default) {
            println("dispatcher: ${this.coroutineContext[ContinuationInterceptor]}")
            println("cancellationAccept - #0")

            var firstReadCanceled = false
            val server =
                Dispatchers.Network.bindTcp(NetworkAddress.Immutable(host = "127.0.0.1", port = 0))
            println("Server start on port ${server.port}")
            val job = launch {
                println("Try accept")
                try {
                    server.accept()
                } catch (e: CancellationException) {
                    firstReadCanceled = true
                } catch (e: Throwable) {
                    e.printStackTrace()
                } finally {
                    server.close()
                }
            }
            realDelay(4.seconds)
            println("Execute cancel!")
            job.cancelAndJoin()
            assertTrue(firstReadCanceled)
        }
    }

    @Test
    fun cancellationRead() = runTest(dispatchTimeoutMs = 5_000) {
        val nd = NetworkCoroutineDispatcherImpl()
        withContext(nd) {
            var firstReadCanceled = false
            val addr = NetworkAddress.Immutable(host = "127.0.0.1", port = TcpServerConnection.randomPort())
            val server = nd.bindTcp(addr)
            var serverShouldSendResponse = false
            launch {
                try {
                    val c2 = server.accept()
                    while (!serverShouldSendResponse) {
                        delay(100)
                    }
                    ByteBuffer.alloc(10).use { buf ->
                        c2.write(buf)
                    }
                } finally {
                    server.close()
                }
            }
            println("Try connect")
            val con = nd.tcpConnect(addr)
            println("Connected!")
            val readJob = launch(nd) {
                ByteBuffer.alloc(10).use { buf ->
                    try {
                        println("Try read data")
                        con.read(buf)
                    } catch (e: CancellationException) {
                        firstReadCanceled = true
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        throw e
                    } finally {
                        println("read try is finished")
                    }
                }
            }
            delay(1000L)
            readJob.cancelAndJoin()
            serverShouldSendResponse = true
            val readJob2 = launch(nd) {
                ByteBuffer.alloc(10).use { buf ->
                    con.read(buf)
                }
            }
            readJob2.join()
            assertTrue(firstReadCanceled, "firstReadCanceled fails")
        }
    }
}
