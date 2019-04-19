package pw.binom.io.socket

import pw.binom.io.ConnectException
import pw.binom.io.UnknownHostException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestClient {
    @Test
    fun `unknown dns host`() {
        val client = Socket()
        val hostName = "unknown_host"
        try {
            client.connect(hostName, 23)
            fail()
        } catch (e: UnknownHostException) {
            assertEquals(e.message, hostName)
            //NOP
        }
    }

    @Test
    fun `invalid port`() {
        val client = Socket()
        val hostName = "127.0.0.1"
        val port = 0xFFFF + 10
        try {
            client.connect(hostName, port)
            fail()
        } catch (e: IllegalArgumentException) {
            assertEquals(e.message, "port out of range:$port")
            //NOP
        }
    }

    @Test
    fun `unknown port`() {
        val client = Socket()
        val hostName = "127.0.0.1"
        val port = 0xFFFF - 1
        try {
            client.connect(hostName, port)
            fail()
        } catch (e: ConnectException) {
            assertEquals(e.message, "$hostName:$port")
            assertEquals(e.host, hostName)
            assertEquals(e.port, port)
        }
    }
}