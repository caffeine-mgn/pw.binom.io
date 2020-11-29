package pw.binom.network

import pw.binom.async
import kotlin.test.Test
import kotlin.test.assertTrue

fun NetworkDispatcher.single(func: suspend () -> Unit) {
    var ex: Throwable? = null
    var done = false
    async {
        try {
            func()
        } catch (e: Throwable) {
            ex = e
        } finally {
            done = true
        }
    }
    while (!done) {
        wait()
    }
    if (ex != null) {
        throw ex!!
    }
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
}

fun modeToString(mode: Int):String {
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