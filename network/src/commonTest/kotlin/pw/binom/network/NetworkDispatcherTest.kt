package pw.binom.network

import kotlinx.coroutines.invoke
import kotlinx.coroutines.runBlocking
import pw.binom.*
import pw.binom.concurrency.*
import pw.binom.coroutine.start
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource
import kotlin.time.measureTime
import kotlin.time.seconds

class AsyncResult {
    var done = false
    var exception: Throwable? = null

    fun finish() {
        exception?.let { throw it }
    }
}

fun asyncRun(func: suspend () -> Unit): AsyncResult {
    val out = AsyncResult()
    async {
        try {
            func()
        } catch (e: Throwable) {
            out.exception = e
        } finally {
            out.done = true
        }
    }
    return out
}

fun NetworkDispatcher.single(func: suspend () -> Unit) {
    this.runSingle(func)
}

class NetworkDispatcherTest {

    @Test
    fun aaa() {
        val nd = NetworkCoroutineDispatcher()
        var connected = false
        runBlocking {
            nd.invoke {
                nd.tcpConnect(NetworkAddress.Immutable("google.com", 443))
                connected = true
            }
        }
        assertTrue(connected)
    }

    @Test
    fun connectTest() {
        val nd = NetworkDispatcher()
        var connected = false
        nd.runSingle {
            nd.tcpConnect(NetworkAddress.Immutable("google.com", 443))
            connected = true
        }
        assertTrue(connected)
    }

    @Test
    fun serverPortGetTest() {
        val nd = NetworkImpl()
        val server = nd.bindTcp(NetworkAddress.Immutable(port = 0))
        assertTrue(server.port > 0)
    }

    @Test
    fun connectionRefusedTest() {
        val nd = NetworkDispatcher()
        var connectionRefused = false
        println("OK!-1")
        nd.runSingle {
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
        }
        println("OK!-7")
        assertTrue(connectionRefused)
    }

    @Test
    fun tcpServerTest() {
        val addr = NetworkAddress.Immutable("0.0.0.0", 0)
        val nd = NetworkDispatcher()
        val server = nd.bindTcp(addr)
        val port = server.port
        try {
            val buf1 = ByteBuffer.alloc(512)
            val buf2 = ByteBuffer.alloc(512)
            Random.nextBytes(buf1)
            buf1.flip()
            nd.runSingle {
                val client = nd.tcpConnect(NetworkAddress.Immutable("127.0.0.1", port))
                val serverClient = server.accept()!!
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
            }
        } finally {
            server.close()
        }
    }

    @Test
    fun rebindTest() {
        val addr = NetworkAddress.Immutable("127.0.0.1", port = 50905)
        val nd = NetworkImpl()
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
    fun udpTest() {
        val address =
            NetworkAddress.Immutable(host = "127.0.0.1", port = Random.nextInt(9999 until (Short.MAX_VALUE - 1) / 2))
        val manager = NetworkImpl()
        println("try bind udp $address")
        val server = manager.bindUDP(address)
        println("binded!")
        val client = manager.openUdp()
        var done = false
        var exception: Throwable? = null
        val request = Random.nextUuid().toString()
        val response = Random.nextUuid().toString()
        async {
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
            }
        }
        while (!done) {
            println("Event!")
            manager.select()
        }
        server.close()
        exception?.let { throw it }
    }


    @OptIn(ExperimentalTime::class)
    @Test
    fun multiThreadingTest() {
        val address =
            NetworkAddress.Immutable(host = "127.0.0.1", port = 0)
        val nd = NetworkDispatcher()
        val server = nd.bindTcp(address)
        val port = server.port
        val executeWorker = WorkerPool(10)
        val serverFuture = async2<Unit> {
            val client = server.accept()!!
            nd.startCoroutine {
                client.readFully(ByteBuffer.alloc(32).clean())
                val clientRef = client.asReference()
                try {
                    executeWorker.start {
                        println("Execute in execute")
                        sleep(1000)
                        nd.start {
                            println("Server write: ${clientRef.value.write(ByteBuffer.wrap(ByteArray(64)).clean())}")
                        }
                    }
                } finally {
                    clientRef.close()
                    client.asyncClose()
                    println("Client done!")
                }
            }
        }
        val clientFuture = async2 {
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
        val now = TimeSource.Monotonic.markNow()
        while (!serverFuture.isDone || !clientFuture.isDone) {
            if (now.elapsedNow() > 10.0.seconds) {
                throw RuntimeException("Timeout")
            }
            nd.select(100)
        }
        if (serverFuture.isFailure) {
            throw serverFuture.exceptionOrNull!!
        }
        if (clientFuture.isFailure) {
            throw clientFuture.exceptionOrNull!!
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun parallelAsync() {
        val address =
            NetworkAddress.Immutable(host = "127.0.0.1", port = Random.nextInt(9999 until (Short.MAX_VALUE - 1) / 2))
        val nd = NetworkDispatcher()
        val server = nd.bindTcp(address)

        async2 {
            try {
                var clientCount = 0
                while (true) {
                    println("Server: try accept")
                    val client = server.accept()!!
                    println("Server: Accepted!")
                    async2 {
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

        fun clientCheck(name: String) = async2 {
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
        val now = TimeSource.Monotonic.markNow()
        while (!clientFuture1.isDone || !clientFuture2.isDone) {
            if (now.elapsedNow() > 10.0.seconds) {
                throw RuntimeException("Timeout")
            }
            nd.select(100)
        }
        if (clientFuture1.isFailure) {
            throw clientFuture1.exceptionOrNull!!
        }
        if (clientFuture2.isFailure) {
            throw clientFuture2.exceptionOrNull!!
        }
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