package pw.binom.io.socket

import kotlin.test.Test
import kotlin.test.assertEquals

class SyncTcpNetSocketTest {
    @Test
    fun connectSuccessTest() {
        val socket = TcpClientNetSocket()
        assertEquals(ConnectStatus.OK, socket.connect(httpServerAddress))
    }

    @Test
    fun alreadyConnectedTest() {
        val socket = TcpClientNetSocket()
        socket.connect(httpServerAddress)
        assertEquals(ConnectStatus.ALREADY_CONNECTED, socket.connect(httpServerAddress))
    }

    @Test
    fun connectRefusedTest() {
        val socket = TcpClientNetSocket()
        assertEquals(ConnectStatus.CONNECTION_REFUSED, socket.connect(InetAddress.resolve("127.0.0.1").withPort(1)))
    }
}
