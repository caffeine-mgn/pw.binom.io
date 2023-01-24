package pw.binom.network

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import pw.binom.io.ByteBuffer
import pw.binom.io.socket.NetworkAddress
import pw.binom.io.use
import pw.binom.thread.Thread
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalCoroutinesApi::class)
class CancellationTest {

    @Test
    @Ignore
    fun sleep() {
        Thread.sleep(10.minutes)
    }

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
                Dispatchers.Network.bindTcp(NetworkAddress.create(host = "127.0.0.1", port = 0))
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

    @OptIn(ExperimentalTime::class)
    @Test
    fun cancellationRead() = runTest(dispatchTimeoutMs = 10_000) {
        val nd = NetworkCoroutineDispatcherImpl()
        withContext(nd) {
            var firstReadCanceled = false
//            val server = nd.bindTcp(NetworkAddress.create("0.0.0.0", 0))
            var serverShouldSendResponse = false
//            launch(nd) {
//                println("Wating client...")
//                server.accept()
//                println("Client connected to server!!!")
//            }

            delay(1.seconds)
            println("Try connect")
            val con = nd.tcpConnect(HTTP_SERVER_ADDRESS)
            println("Connected!  $con")
            val readJob = launch(nd) {
                println("Task executed!")
                ByteBuffer(10).use { buf ->
                    try {
                        println("Try read data...")
                        val bb = con.read(buf)
                        println("done! $bb")
                    } catch (e: CancellationException) {
                        firstReadCanceled = true
                    } catch (e: Throwable) {
                        println("Exception happened: $e")
//                        throw e
                    } finally {
                        println("read try is finished")
                    }
                }
            }
            val mm = TimeSource.Monotonic.markNow()
            println("wait one second")
            delay(2.seconds)
            println("Try cancel read   ${mm.elapsedNow()}")
            readJob.cancelAndJoin()
            serverShouldSendResponse = true
            assertTrue(firstReadCanceled, "firstReadCanceled fails")
        }
    }
}
