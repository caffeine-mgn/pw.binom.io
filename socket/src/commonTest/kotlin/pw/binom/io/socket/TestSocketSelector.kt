package pw.binom.io.socket

import pw.binom.thread.Thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class TestSocketSelector {

    @Test
    fun `unbind after disconnect`() {
        val port = 9912
        val server = RawSocketServer()
        server.bind("127.0.0.1", port)
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

    @Test
    fun `reattach`() {
        val selector = SocketSelector(100)
        val client = RawSocketChannel()
        client.blocking = false
        val b1 = Any()
        val k = selector.reg(client, b1).apply {
            assertTrue(listenReadable)
            assertTrue(listenWritable)
        }
        assertSame(b1, k.attachment)
        k.cancel()
        val b2 = Any()
        val v = selector.reg(client,b2).apply {
            assertTrue(listenReadable)
            assertTrue(listenWritable)
        }

        assertSame(k, v)
        assertSame(b2, k.attachment)

        selector.process(1) {

        }

        k.apply {
            assertTrue(listenReadable)
            assertTrue(listenWritable)
        }
    }
}