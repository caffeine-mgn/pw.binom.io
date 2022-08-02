package pw.binom.network

import pw.binom.thread.Thread
import kotlin.random.Random
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

class SelectorTest {

    @Test
    fun wakeupTest() {
        val selector = Selector.open()
        val selectKeys = SelectedEvents.create()
        Thread {
            Thread.sleep(1000)
            selector.wakeup()
        }.start()
        val it = selector.select(selectedEvents = selectKeys)
        println("Count: $it")
        selectKeys.forEach {
            fail()
        }
    }

    @Test
    fun selectTimeoutTest1() {
        val selectKeys = SelectedEvents.create()
        val selector = Selector.open()
        val it = selector.select(1000, selectKeys)
        assertEquals(0, it)
        assertFalse(selectKeys.iterator().hasNext())
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun selectTimeoutTest2() {
        val selectKeys = SelectedEvents.create()
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        client.setBlocking(false)
        selector.attach(client)
        val beforeSelecting = TimeSource.Monotonic.markNow()
        val count = selector.select(1000, selectKeys)
        assertEquals(0, count)
        val it = selectKeys.iterator()
        val selectingTime = beforeSelecting.elapsedNow()
        assertTrue(selectingTime > 0.5.seconds && selectingTime < 1.5.seconds)
        assertFalse(it.hasNext())
    }

    @Test
    fun connectTest2() {
        val selectKeys = SelectedEvents.create()
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        client.setBlocking(false)
        val key = selector.attach(client)
        try {
            key.listensFlag = 0
            client.connect(NetworkAddress.Immutable("google.com", 443))
            selector.select(2000, selectKeys)
            assertFalse(selectKeys.iterator().hasNext())
        } finally {
            key.close()
            client.close()
            selector.close()
        }
    }

    @Test
    fun connectTest() {
        val selectKeys = SelectedEvents.create()
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        client.setBlocking(false)
        val key = selector.attach(client)
        println("try set flags...  ${Selector.EVENT_CONNECTED}")
        key.listensFlag = Selector.EVENT_CONNECTED
        println("Flag setted")
        client.connect(NetworkAddress.Immutable("google.com", 443))
        selector.select(5000, selectKeys)
        var it = selectKeys.iterator()
        assertTrue(it.hasNext())
        val mode = it.next().mode
        assertTrue(mode and Selector.EVENT_CONNECTED != 0)
        assertTrue(mode and Selector.OUTPUT_READY != 0)
//        assertEquals(1, selector.select(5000) { key, mode ->
//            println("--1")
//            assertTrue(mode and Selector.EVENT_CONNECTED != 0)
//            println("--2")
//            assertTrue(mode and Selector.OUTPUT_READY != 0)
//            println("--3")
//        })
        selector.select(1000, selectKeys)
        it = selectKeys.iterator()
        assertFalse(it.hasNext())
    }

    @Test
    fun connectionRefusedTest() {
        val selectKeys = SelectedEvents.create()
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        client.setBlocking(false)
        selector.attach(client)
        client.connect(NetworkAddress.Immutable("127.0.0.1", 12))

        selector.select(5000, selectKeys)
        selectKeys.forEach {
            val mode = it.mode
            val key = it.key
            if (mode and Selector.INPUT_READY != 0) {
                println("Selector.INPUT_READY")
            }
            if (mode and Selector.OUTPUT_READY != 0) {
                println("Selector.OUTPUT_READY")
            }
            if (mode and Selector.EVENT_CONNECTED != 0) {
                println("Selector.EVENT_CONNECTED")
            }
            if (mode and Selector.EVENT_ERROR != 0) {
                println("Selector.EVENT_ERROR")
            }
            assertTrue(mode and Selector.EVENT_ERROR != 0)
            client.close()
        }
        assertEquals(0, selector.select(1000, selectKeys))
    }

    @Test
    fun bindTest() {
        val selectKeys = SelectedEvents.create()
        val selector = Selector.open()
        val server = TcpServerSocketChannel()
        val addr = NetworkAddress.Immutable("0.0.0.0", Random.nextInt(1000, 5999))
        server.bind(addr)
        server.setBlocking(false)
        selector.attach(server)
        assertEquals(0, selector.select(1000, selectKeys))
    }
}
