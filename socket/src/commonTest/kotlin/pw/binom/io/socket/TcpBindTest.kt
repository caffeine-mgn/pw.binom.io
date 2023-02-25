package pw.binom.io.socket

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TcpBindTest {
    @Test
    fun bindRandomPortTest() {
        val socket = Socket.createTcpServerNetSocket()
        assertEquals(BindStatus.OK, socket.bind(NetworkAddress.create("127.0.0.1", 0)))
        assertNotEquals(null, socket.port)
        assertNotEquals(0, socket.port)
    }

    @Test
    fun alreadyBindTest() {
        val socket = Socket.createTcpServerNetSocket()
        socket.bind(NetworkAddress.create("127.0.0.1", 0))
        assertEquals(BindStatus.ALREADY_BINDED, socket.bind(NetworkAddress.create("127.0.0.1", 0)))
    }

    @Test
    fun bindBindedTest() {
        val socket1 = Socket.createTcpServerNetSocket()
        socket1.bind(NetworkAddress.create("127.0.0.1", 0))
        val socket2 = Socket.createTcpServerNetSocket()
        assertEquals(
            BindStatus.ADDRESS_ALREADY_IN_USE,
            socket2.bind(NetworkAddress.create("127.0.0.1", socket1.port!!)),
        )
    }

    @Test
    fun bindUnixSocket() {
        val socket = Socket.createTcpServerUnixSocket()
        assertEquals(BindStatus.OK, socket.bind("test_sock1"))
        assertEquals(BindStatus.ALREADY_BINDED, socket.bind("test_sock1"))
    }
}
