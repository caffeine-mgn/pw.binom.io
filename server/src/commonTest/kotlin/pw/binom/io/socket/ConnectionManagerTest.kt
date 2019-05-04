package pw.binom.io.socket

import pw.binom.io.readln
import kotlin.test.Test
import kotlin.test.assertEquals

class ConnectionManagerTest {

    @Test
    fun test() {
        val port = 9919
        val manager = object : ConnectionManager() {
            override fun connected(connection: Connection) {
                connection {
                    it.input.readln()
                }
            }
        }
        manager.bind(port)
        val soc = Socket()
        soc.connect("127.0.0.1", port)
        manager.update(1)
        assertEquals(2, manager.clientSize)
        manager.update(1)
        soc.close()
        manager.update(1)
        assertEquals(1, manager.clientSize)
    }
}