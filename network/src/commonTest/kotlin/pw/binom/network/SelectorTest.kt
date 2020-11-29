package pw.binom.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectorTest {

    @Test
    fun timeoutTest1() {
        val selector = Selector()

        assertFalse(selector.wait(1000) { _, mode ->
            println("mode: $mode")
        })
    }

    @Test
    fun timeoutTest2() {
        val selector = Selector()
        val client = TcpClientSocketChannel()
        selector.attach(client)

        assertFalse(selector.wait(1000) { _, mode ->
            println("mode: $mode")
        })
    }

    @Test
    fun connectTest() {
        val selector = Selector()
        val client = TcpClientSocketChannel()
        selector.attach(client)
        client.connect(NetworkAddress.Immutable("google.com", 443))

        assertTrue(selector.wait(1000) { attaching, mode ->
            println("Mode: $mode")
            assertTrue(mode and Selector.EVENT_CONNECTED != 0)
        })

        assertFalse(selector.wait(1000) { _, _ -> })
    }

    @Test
    fun connectTest2() {
        val selector = Selector()
        val client = TcpClientSocketChannel()
        selector.attach(client, Selector.EVENT_CONNECTED)
        client.connect(NetworkAddress.Immutable("127.0.0.1", 12))

        assertTrue(selector.wait() { attaching, mode ->
            println("Mode: $mode")
            assertTrue(mode and Selector.EVENT_ERROR != 0)
        })
        assertFalse(selector.wait(1000) { _, _ -> })
    }
}