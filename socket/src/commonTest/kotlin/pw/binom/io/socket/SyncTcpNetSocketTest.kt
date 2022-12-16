package pw.binom.io.socket

import kotlin.test.Test
import kotlin.test.assertEquals

class SyncTcpNetSocketTest {
    @Test
    fun connectSuccessTest() {
        val socket = Socket.createTcpClientNetSocket()
        assertEquals(ConnectStatus.OK, socket.connect(httpServerAddress))
    }

    @Test
    fun alreadyConnectedTest() {
        val socket = Socket.createTcpClientNetSocket()
        socket.connect(httpServerAddress)
        assertEquals(ConnectStatus.ALREADY_CONNECTED, socket.connect(httpServerAddress))
    }

    @Test
    fun connectRefusedTest() {
        val socket = Socket.createTcpClientNetSocket()
        assertEquals(ConnectStatus.CONNECTION_REFUSED, socket.connect(NetworkAddress.create("127.0.0.1", 1)))
    }
}
