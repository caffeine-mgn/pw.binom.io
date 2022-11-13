package pw.binom.network

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import pw.binom.concurrency.WorkerPool
import pw.binom.io.ByteBuffer
import pw.binom.io.clean
import pw.binom.io.nextBytes
import pw.binom.nextUuid
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkDispatcherTest {

    @Test
    fun aaa() = runTest(dispatchTimeoutMs = 5_000) {
        val nd = NetworkCoroutineDispatcherImpl()
        nd.tcpConnect(NetworkAddress.Immutable("google.com", 443))
    }

    @Test
    fun connectTest() = runTest {
        val nd = NetworkCoroutineDispatcherImpl()
        nd.tcpConnect(NetworkAddress.Immutable("google.com", 443))
    }

    @Test
    fun serverPortGetTest() = runTest {
        val nd = NetworkCoroutineDispatcherImpl()
        val server = nd.bindTcp(NetworkAddress.Immutable(port = 0))
        assertTrue(server.port > 0)
    }

    @Test
    fun connectionRefusedTest() = runTest {
        val nd = NetworkCoroutineDispatcherImpl()
        var connectionRefused = false
        println("OK!-1")
        println("OK!-2")
        try {
            println("OK!-3")
            nd.tcpConnect(NetworkAddress.Immutable("127.0.0.1", 12))
            println("OK!-4")
            fail("Invalid state")
        } catch (e: SocketConnectException) {
            println("OK!-5")
            println("Error: ${e is SocketConnectException}")
            e.printStackTrace()
            connectionRefused = true
        }
        println("OK!-6")
        println("OK!-7")
        assertTrue(connectionRefused)
    }

    @Test
    fun tcpServerTest() = runTest {
        val addr = NetworkAddress.Immutable("0.0.0.0", 0)
        val nd = NetworkCoroutineDispatcherImpl()
        val server = nd.bindTcp(addr)
        val port = server.port
        try {
            val buf1 = ByteBuffer.alloc(512)
            val buf2 = ByteBuffer.alloc(512)
            Random.nextBytes(buf1)
            buf1.flip()
            val client = nd.tcpConnect(NetworkAddress.Immutable("127.0.0.1", port))
            val serverClient = server.accept()
            client.write(buf1)
            serverClient.readFully(buf2)
            buf2.flip()
            buf1.flip()
            for (i in 0 until buf1.capacity) {
                assertEquals(buf1[i], buf2[i])
            }
            client.write(buf1)
            serverClient.readFully(buf2)
            buf2.flip()
            buf1.flip()
            for (i in 0 until buf1.capacity) {
                assertEquals(buf1[i], buf2[i])
            }
        } finally {
            server.close()
        }
    }

    @Test
    fun rebindTest() = runTest {
        val addr = NetworkAddress.Immutable("127.0.0.1", port = 0)
        val nd = NetworkCoroutineDispatcherImpl()
        val a = nd.bindTcp(addr)
        val port = a.port
        assertTrue(port > 0)
        try {
            nd.bindTcp(NetworkAddress.Immutable("127.0.0.1", port = port))
            fail("Port rebind success on 127.0.0.1:$port")
        } catch (e: BindException) {
            //
        } finally {
            a.close()
        }
    }

    @Test
    fun udpTest() = runTest {
        println("Try find free port")
        val address =
            NetworkAddress.Immutable(host = "127.0.0.1", port = UdpConnection.randomPort())
        val manager = NetworkCoroutineDispatcherImpl()
        println("try bind udp $address")
        val server = manager.bindUdp(address)
        println("binded!")
        val client = manager.attach(UdpSocketChannel())
        var done = false
        var exception: Throwable? = null
        val request = Random.nextUuid().toString()
        val response = Random.nextUuid().toString()
        try {
            val buf = ByteBuffer.alloc(512)
            val addr = NetworkAddress.Mutable()
            client.write(
                ByteBuffer.wrap(request.encodeToByteArray()),
                NetworkAddress.Immutable("127.0.0.1", address.port)
            )
            server.read(buf, addr)
            buf.flip()
            assertEquals(request, buf.toByteArray().decodeToString())

            server.write(ByteBuffer.wrap(response.encodeToByteArray()), addr)
            buf.clear()
            client.read(buf, null)
            buf.flip()
            assertEquals(response, buf.toByteArray().decodeToString())
        } catch (e: Throwable) {
            e.printStackTrace()
            exception = e
        } finally {
            done = true
            server.close()
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun multiThreadingTest() = runTest(dispatchTimeoutMs = 10_000) {
        val address =
            NetworkAddress.Immutable(host = "127.0.0.1", port = 0)
        val nd = NetworkCoroutineDispatcherImpl()
        val server = nd.bindTcp(address)
        val port = server.port
        val executeWorker = WorkerPool(10)
        val serverFuture = GlobalScope.launch(nd) {
            val client = server.accept()
            launch {
                client.readFully(ByteBuffer.alloc(32).clean())
                try {
                    withContext(executeWorker) {
                        println("Execute in execute")
                        delay(1000)
                        launch {
                            println("Server write: ${client.write(ByteBuffer.wrap(ByteArray(64)).clean())}")
                        }
                    }
                } finally {
                    client.asyncClose()
                    println("Client done!")
                }
            }
        }
        val clientFuture = GlobalScope.launch {
            println("Connection...")
            val client2 = nd.tcpConnect(NetworkAddress.Immutable(host = "127.0.0.1", port = port))
            println("Connected! Write...")
            client2.write(ByteBuffer.wrap(ByteArray(32)).clean())
            println("Wrote! Try read...")
            val readTime = measureTime {
                client2.readFully(ByteBuffer.alloc(64).clean())
            }
            assertTrue(readTime > 1.0.seconds && readTime < 2.0.seconds)
            println("Readed!")
            client2.asyncClose()
            println("Closed!")
        }
        serverFuture.join()
        clientFuture.join()
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun parallelAsync() = runTest(dispatchTimeoutMs = 10_000) {
        val address =
            NetworkAddress.Immutable(host = "127.0.0.1", port = Random.nextInt(9999 until (Short.MAX_VALUE - 1) / 2))
        val nd = NetworkCoroutineDispatcherImpl()
        val server = nd.bindTcp(address)

        GlobalScope.launch(nd) {
            try {
                var clientCount = 0
                while (true) {
                    println("Server: try accept")
                    val client = server.accept()
                    println("Server: Accepted!")
                    launch {
                        clientCount++
                        println("Server: Client connected! Count: $clientCount")
                        try {
                            println("Server: readed ${client.readFully(ByteBuffer.alloc(32).clean())}")
                            println("Server: wrote ${client.write(ByteBuffer.wrap(ByteArray(64)).clean())}")
                        } catch (e: Throwable) {
                            e.printStackTrace()
                            throw e
                        } finally {
                            client.asyncClose()
                            println("Server: Client done! Client Count: $clientCount")
                            clientCount--
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }

        fun clientCheck(name: String) = GlobalScope.launch(nd) {
            try {
                val client2 = nd.tcpConnect(address)
                println("Client[$name-${client2.hashCode()}]:Connected! Write...")
                println(
                    "Client[$name-${client2.hashCode()}]: Wrote ${
                    client2.write(
                        ByteBuffer.wrap(ByteArray(32)).clean()
                    )
                    }"
                )
                println("Client[$name-${client2.hashCode()}]:Wrote! Try read...")
                val readTime = measureTime {
                    println(
                        "Client[$name-${client2.hashCode()}]: read ${
                        client2.readFully(
                            ByteBuffer.alloc(64).clean()
                        )
                        }"
                    )
                }
//            assertTrue(readTime > 1.0.seconds && readTime < 2.0.seconds)
                println("Client[$name-${client2.hashCode()}]:Readed!")
                client2.asyncClose()
                println("Client[$name-${client2.hashCode()}]:Closed!")
            } catch (e: Throwable) {
                e.printStackTrace()
                throw e
            }
        }

        val clientFuture1 = clientCheck("1")
        val clientFuture2 = clientCheck("2")
        clientFuture1.join()
        clientFuture2.join()
    }
}

fun modeToString(mode: Int): String {
    val sb = StringBuilder()
    if (Selector.OUTPUT_READY and mode != 0) {
        sb.append("EVENT_EPOLLOUT ")
    }

    if (Selector.INPUT_READY and mode != 0) {
        sb.append("EVENT_EPOLLIN ")
    }

    if (Selector.EVENT_CONNECTED and mode != 0) {
        sb.append("EVENT_CONNECTED ")
    }

    if (Selector.EVENT_ERROR and mode != 0) {
        sb.append("EVENT_ERROR ")
    }
    return sb.toString()
}
