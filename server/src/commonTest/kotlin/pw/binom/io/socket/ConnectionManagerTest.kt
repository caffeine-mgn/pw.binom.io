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
        val manager = object : ConnectionManager() {
            override fun connected(connection: Connection) {
                connection {
                    try {
                        it.input.readln()
                        fail()
                    } catch (e: IOException) {
                        done=true
                    }
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
        assertEquals(2, manager.clientSize)
        assertTrue(done)
    }
}