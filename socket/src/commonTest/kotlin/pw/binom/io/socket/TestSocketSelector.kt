package pw.binom.io.socket

import pw.binom.concurrency.Worker
import pw.binom.concurrency.sleep
import kotlin.random.Random
import kotlin.test.*

class TestSocketSelector {

    @Test
    fun `unbind after disconnect`() {
        val port = Random.nextInt(20, 0xFFF)
        val server = RawSocketServer()
        server.bind("127.0.0.1", port)
        Worker.sleep(100)

        val selector = SocketSelector()

        val client = RawSocketChannel()
        client.blocking = false
        selector.reg(client)
        client.connect(NetworkAddress.create("127.0.0.1", port))
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

    @Ignore
    @Test
    fun `reattach`() {
        val selector = SocketSelector()
        val client = RawSocketChannel()
        client.blocking = false
        val b1 = Any()
        val k = selector.reg(client, b1).apply {
            assertFalse(listenReadable)
            assertFalse(listenWritable)
        }
        assertSame(b1, k.attachment)
        k.cancel()
        val b2 = Any()
        val v = selector.reg(client, b2).apply {
            assertFalse(listenReadable)
            assertFalse(listenWritable)
        }

        assertSame(k, v)
        assertSame(b2, k.attachment)

        selector.process(1) {

        }

        k.apply {
            assertFalse(listenReadable)
            assertFalse(listenWritable)
        }
    }
}