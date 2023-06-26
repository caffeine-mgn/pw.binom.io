package pw.binom.network

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import pw.binom.io.ByteBuffer
import pw.binom.io.clean
import pw.binom.io.nextBytes
import pw.binom.io.socket.MutableInetNetworkAddress
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.io.socket.Socket
import pw.binom.io.wrap
import pw.binom.uuid.nextUuid
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
        nd.tcpConnect(InetNetworkAddress.create("google.com", 443))
    }

    @Test
    fun connectTest() = runTest {
        val nd = NetworkCoroutineDispatcherImpl()
        nd.tcpConnect(InetNetworkAddress.create("google.com", 443))
    }

    @Test
    fun serverPortGetTest() = runTest {
        val nd = NetworkCoroutineDispatcherImpl()
        val server = nd.bindTcp(InetNetworkAddress.create(port = 0, host = "0.0.0.0"))
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
            nd.tcpConnect(InetNetworkAddress.create("127.0.0.1", 12))
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
        val addr = InetNetworkAddress.create("0.0.0.0", 0)
        val nd = NetworkCoroutineDispatcherImpl()
        val server = nd.bindTcp(addr)
        val port = server.port
        try {
            val buf1 = ByteBuffer(512)
            val buf2 = ByteBuffer(512)
            Random.nextBytes(buf1)
            buf1.flip()
            val client = nd.tcpConnect(InetNetworkAddress.create("127.0.0.1", port))
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
        val addr = InetNetworkAddress.create("127.0.0.1", port = 0)
        val nd = NetworkCoroutineDispatcherImpl()
        val a = nd.bindTcp(addr)
        val port = a.port
        assertTrue(port > 0)
        try {
            nd.bindTcp(InetNetworkAddress.create("127.0.0.1", port = port))
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
        val address = InetNetworkAddress.create(host = "127.0.0.1", port = UdpConnection.randomPort())
        val manager = NetworkCoroutineDispatcherImpl()
        println("try bind udp $address")
        val server = manager.bindUdp(address)
        println("binded!")
        val client = manager.attach(Socket.createUdpNetSocket())
        var done = false
        var exception: Throwable? = null
        val request = Random.nextUuid().toString()
        val response = Random.nextUuid().toString()
        try {
            val buf = ByteBuffer(512)
            val addr = MutableInetNetworkAddress.create()
            client.write(
                request.encodeToByteArray().wrap(),
                InetNetworkAddress.create(host = "127.0.0.1", port = address.port)
            )
            server.read(buf, addr)
            buf.flip()
            assertEquals(request, buf.toByteArray().decodeToString())

            server.write(response.encodeToByteArray().wrap(), addr)
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
            InetNetworkAddress.create(host = "127.0.0.1", port = 0)
        val nd = NetworkCoroutineDispatcherImpl()
        val server = nd.bindTcp(address)
        val port = server.port
        val serverFuture = GlobalScope.launch(nd) {
            val client = server.accept()
            launch {
                client.readFully(ByteBuffer(32).clean())
                try {
                    withContext(ThreadCoroutineDispatcher()) {
                        println("Execute in execute")
                        delay(1000)
                        launch {
                            println("Server write: ${client.write(ByteArray(64).wrap().clean())}")
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
            val client2 = nd.tcpConnect(InetNetworkAddress.create(host = "127.0.0.1", port = port))
            println("Connected! Write...")
            client2.write(ByteArray(32).wrap().clean())
            println("Wrote! Try read...")
            val readTime = measureTime {
                client2.readFully(ByteBuffer(64).clean())
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
            InetNetworkAddress.create(host = "127.0.0.1", port = Random.nextInt(9999 until (Short.MAX_VALUE - 1) / 2))
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
                            println("Server: readed ${client.readFully(ByteBuffer(32).clean())}")
                            println("Server: wrote ${client.write(ByteArray(64).wrap().clean())}")
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
                val w = client2.write(ByteArray(32).wrap())
                println("Client[$name-${client2.hashCode()}]: Wrote $w")
                println("Client[$name-${client2.hashCode()}]:Wrote! Try read...")
                val readTime = measureTime {
                    val w = client2.readFully(
                        ByteBuffer(64).clean()
                    )
                    println("Client[$name-${client2.hashCode()}]: read $w")
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
