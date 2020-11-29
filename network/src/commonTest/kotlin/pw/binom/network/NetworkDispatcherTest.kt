package pw.binom.network

import pw.binom.ByteBuffer
import pw.binom.async
import pw.binom.nextBytes
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        wait()
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
        val addr = NetworkAddress.Immutable("127.0.0.1", 13)
        val nd = NetworkDispatcher()
        val server = nd.bindTcp(addr)
        val buf1 = ByteBuffer.alloc(512)
        val buf2 = ByteBuffer.alloc(512)
        Random.nextBytes(buf1)
        buf1.flip()
        nd.single {
            val client = nd.tcpConnect(addr)
            val serverClient = server.accept()!!
            client.write(buf1)
            serverClient.readFully(buf2)
            buf2.flip()
            buf1.flip()
            for (i in 0 until buf1.capacity) {
                assertEquals(buf1[i], buf2[i])
            }
        }
    }
}

fun modeToString(mode: Int): String {
    val sb = StringBuilder()
    if (Selector.EVENT_EPOLLOUT and mode != 0) {
        sb.append("EVENT_EPOLLOUT ")
    }

    if (Selector.EVENT_EPOLLIN and mode != 0) {
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