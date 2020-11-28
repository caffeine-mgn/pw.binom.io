package pw.binom.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectorTest {

    @Test
    fun timeoutTest() {
        val selector = Selector()
        val client = TcpClientSocketChannel()
        selector.attach(client)

        assertFalse(selector.wait(1000) { _, _ -> })
    }

    @Test
    fun connectTest() {
        val selector = Selector()
        val client = TcpClientSocketChannel()
        selector.attach(client, Selector.EVENT_CONNECTED)
        client.connect(NetworkAddress.Immutable("google.com", 80))

        assertTrue(selector.wait(1000) { attaching, mode ->
            println("Mode: $mode")
            assertEquals(Selector.EVENT_CONNECTED, mode)
        })
    }
}