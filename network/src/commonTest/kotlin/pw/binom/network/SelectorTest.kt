package pw.binom.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectorTest {

    @Test
    fun selectTimeoutTest1() {
        val selector = Selector.open()
        assertEquals(0, selector.select(1000) { _, _ -> })
    }

    @Test
    fun selectTimeoutTest2() {
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        selector.attach(client)
        assertEquals(0, selector.select(1000) { _, _ -> })
    }

    @Test
    fun connectTest() {
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        selector.attach(client)
        client.connect(NetworkAddress.Immutable("google.com", 443))

        assertEquals(1, selector.select(1000) { key, mode ->
            println("Mode: ${modeToString(mode)} ${mode.toString(2)}")
            println("EVENT_CONNECTED--->${mode and Selector.EVENT_CONNECTED}")
            assertTrue(mode and Selector.EVENT_CONNECTED != 0)
            assertTrue(mode and Selector.EVENT_EPOLLOUT != 0)
        })

        assertEquals(0, selector.select(1000) { _, _ -> })
    }

    @Test
    fun connectionRefusedTest() {
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        selector.attach(client)
        client.connect(NetworkAddress.Immutable("127.0.0.1", 12))

        selector.select(5000) { key, mode ->
            println("Mode: ${modeToString(mode)} $mode")
            assertTrue(mode and Selector.EVENT_ERROR != 0)
            client.close()
        }
        assertEquals(0, selector.select(1000) { _, _ -> })
    }
}