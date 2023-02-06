package pw.binom.socket

import pw.binom.io.socket.KeyListenFlags
import pw.binom.io.socket.NetworkAddress
import pw.binom.io.socket.Selector
import pw.binom.io.socket.Socket
import pw.binom.thread.Thread
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class NativeSelectorTest {
    @Test
    fun removeKeyNowTest() {
        val selector = Selector()
        val server = Socket.createTcpServerNetSocket()
        server.bind(NetworkAddress.create(host = "127.0.0.1", port = 0))
        val key = selector.attach(server)
        key.listenFlags = KeyListenFlags.READ or KeyListenFlags.ERROR
        key.close()
        assertFalse(key in selector.keyForRemove)
        var eventCount = 0
        selector.select(timeout = 3.seconds) {
            eventCount++
        }
        assertEquals(0, eventCount)
        println("Hello! $key")
    }

    @Test
    fun removeLaterTest() {
        val selector = Selector()
        val server = Socket.createTcpServerNetSocket()
        server.bind(NetworkAddress.create(host = "127.0.0.1", port = 0))
        val key = selector.attach(server)
        key.listenFlags = KeyListenFlags.READ or KeyListenFlags.ERROR or KeyListenFlags.ONCE
        var count = 0

        Thread {
            selector.select(timeout = 3.seconds) {}
        }.start()
        Thread.sleep(1.seconds)
        key.close()
        assertEquals(1, selector.keyForRemove.size)
        assertTrue(key in selector.keyForRemove)
        selector.wakeup()
        Thread.sleep(1.seconds)
        selector.select(timeout = 1.seconds) { fail() }
        assertEquals(0, selector.keyForRemove.size)
    }
}
