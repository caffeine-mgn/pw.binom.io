package pw.binom.network

import pw.binom.*
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

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
    run(asyncRun(func))
}

fun NetworkDispatcher.run(result: AsyncResult) {
    while (!result.done) {
        select()
    }
    result.finish()
}

class NetworkDispatcherTest {

    @Test
    fun connectTest() {
        val nd = NetworkDispatcher()
        var connected = false
        nd.single {
            nd.tcpConnect(NetworkAddress.Immutable("google.com", 443))
            connected = true
        }
        assertTrue(connected)
    }

    @Test
    fun connectionRefusedTest() {
        val nd = NetworkDispatcher()
        var connectionRefused = false
        nd.single {
            try {
                nd.tcpConnect(NetworkAddress.Immutable("127.0.0.1", 12))
            } catch (e: SocketConnectException) {
                connectionRefused = true
            }
        }
        assertTrue(connectionRefused)
    }

    @Test
    fun tcpServerTest() {
        val addr = NetworkAddress.Immutable("0.0.0.0", 9967)
        val nd = NetworkDispatcher()
        val server = nd.bindTcp(addr)
        try {
            val buf1 = ByteBuffer.alloc(512)
            val buf2 = ByteBuffer.alloc(512)
            Random.nextBytes(buf1)
            buf1.flip()
            nd.single {
                val client = nd.tcpConnect(NetworkAddress.Immutable("127.0.0.1", 9967))
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
        val addr = NetworkAddress.Immutable("0.0.0.0", Random.nextInt(1000, 5999))
        val nd = NetworkDispatcher()
        val a = nd.bindTcp(addr)
        try {
            nd.bindTcp(addr)
            fail()
        } catch (e: BindException) {
            //
        } finally {
            a.close()
        }
    }

    @Test
    fun udpTest() {
        val address = NetworkAddress.Immutable(port = Random.nextInt(9999 until Short.MAX_VALUE-1))
        val manager = NetworkDispatcher()
        val server = manager.bindUDP(address)
        val client = manager.openUdp()
        var done = false
        var exception: Throwable? = null
        val request = Random.uuid().toString()
        val response = Random.uuid().toString()
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
                client.read(buf,null)
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