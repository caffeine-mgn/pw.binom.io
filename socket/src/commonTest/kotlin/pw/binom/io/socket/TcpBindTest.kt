package pw.binom.io.socket

import pw.binom.Environment
import pw.binom.Platform
import pw.binom.platform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TcpBindTest {
    @Test
    fun bindRandomPortTest() {
        val socket = Socket.createTcpServerNetSocket()
        assertEquals(BindStatus.OK, socket.bind(InetNetworkAddress.create("127.0.0.1", 0)))
        assertNotEquals(null, socket.port)
        assertNotEquals(0, socket.port)
    }

    @Test
    fun alreadyBindTest() {
        val socket = Socket.createTcpServerNetSocket()
        socket.bind(InetNetworkAddress.create("127.0.0.1", 0))
        assertEquals(BindStatus.ALREADY_BINDED, socket.bind(InetNetworkAddress.create("127.0.0.1", 0)))
    }

    @Test
    fun bindBindedTest() {
        val socket1 = Socket.createTcpServerNetSocket()
        socket1.bind(InetNetworkAddress.create("127.0.0.1", 0))
        val socket2 = Socket.createTcpServerNetSocket()
        assertEquals(
            BindStatus.ADDRESS_ALREADY_IN_USE,
            socket2.bind(InetNetworkAddress.create("127.0.0.1", socket1.port!!)),
        )
    }

    @Test
    fun bindUnixSocket() {
        if (Environment.platform == Platform.MINGW_X86 || Environment.platform == Platform.MINGW_X64) {
            return
        }
        val socket = Socket.createTcpServerUnixSocket()
        assertEquals(BindStatus.OK, socket.bind("test_sock1"))
        assertEquals(BindStatus.ALREADY_BINDED, socket.bind("test_sock1"))
    }
}
