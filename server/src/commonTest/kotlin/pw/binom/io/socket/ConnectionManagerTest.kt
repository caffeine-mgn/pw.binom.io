package pw.binom.io.socket

import pw.binom.io.IOException
import pw.binom.io.readln
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class ConnectionManagerTest {

    @Test
    fun testBind() {
        val port = 9919
        var done = false
        val handler = object : ConnectionManager.ConnectHandler {
            override fun clientConnected(connection: ConnectionManager.ConnectionRaw, manager: ConnectionManager) {
                connection {
                    try {
                        it.input.readln()
                        fail()
                    } catch (e: IOException) {
                        done = true
                    }
                }
            }
        }
        val manager = ConnectionManager()
        manager.bind(port = port, handler = handler)
        val soc = RawSocket()
        soc.connect("127.0.0.1", port)
        manager.update(1)
        assertEquals(2, manager.clientSize)
        manager.update(1)
        soc.close()
        manager.update(1)
        assertEquals(2, manager.clientSize)
        assertTrue(done)
    }
}