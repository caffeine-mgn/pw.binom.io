package pw.binom.io.socket

import pw.binom.Thread
import kotlin.test.Test
import kotlin.test.assertEquals

class TestSocketSelector {

    @Test
    fun `unbind after disconnect`() {
        val port = 9912
        val server = RawSocketServer()
        server.bind("127.0.0.1",port)
        Thread.sleep(100)

        val selector = SocketSelector(100)

        val client = RawSocketChannel()
        client.blocking = false
        selector.reg(client)
        client.connect("127.0.0.1", port)
        val serverClient = server.accept()!!
        assertEquals(1, selector.keys.size)
        selector.process(1) {
        }
        serverClient.close()
        selector.process(1) {
        }
        assertEquals(1, selector.keys.size)
        client.close()
        assertEquals(1, selector.keys.size)
    }
}