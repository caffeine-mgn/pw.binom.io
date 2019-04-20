package pw.binom.io.socket

import pw.binom.io.BindException
import pw.binom.io.SocketException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestServer {
    @Test
    fun `invalid port`() {
        val port = 0xFFFF + 10
        val server = SocketServer()
        try {
            server.bind(port)
        } catch (e: IllegalArgumentException) {
            assertEquals(e.message, "port out of range:$port")
            //NOP
        }
    }

    @Test
    fun `bind binded port`() {
        val port = 0xFFFF - 10
        val server1 = SocketServer()
        val server2 = SocketServer()
        server1.bind(port)
        try {
            server2.bind(port)
            fail()
        } catch (e: BindException) {
            //NOP
        }
        server1.close()
        server2.close()
    }

    @Test
    fun `rebind`() {
        val port = 0xFFFF - 10
        val server = SocketServer()
        server.bind(port)
        try {
            server.bind(port)
            fail()
        } catch (e: SocketException) {
            //NOP
        }
        server.close()
    }
}