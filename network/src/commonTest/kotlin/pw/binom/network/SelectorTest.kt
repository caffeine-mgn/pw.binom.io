package pw.binom.network

import pw.binom.io.ByteBuffer
import pw.binom.io.use
import pw.binom.thread.Thread
import kotlin.random.Random
import kotlin.test.*
import kotlin.time.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
        key.listensFlag = Selector.EVENT_CONNECTED
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
        selectKeys.iterator().forEach {
            println("->$it")
        }
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

    @Test
    fun testSeveralKeyOfOneSocket() {
        val server = TcpServerSocketChannel()
        server.setBlocking(false)
        server.bind(NetworkAddress.Immutable(host = "127.0.0.1", port = 0))
        val selectKeys1 = SelectedEvents.create()
        val selector1 = Selector.open()
        println("#1")
        val key1 = selector1.attach(server)
        println("#2")
        key1.listensFlag = Selector.INPUT_READY
        val selectKeys2 = SelectedEvents.create()
        val selector2 = Selector.open()
        println("#3")
        val key2 = selector2.attach(server)
        println("#4")
        key2.listensFlag = Selector.INPUT_READY
        TcpClientSocketChannel().connect(NetworkAddress.Immutable(host = "127.0.0.1", port = server.port!!))

        val v1 = selector1.select(timeout = 10, selectedEvents = selectKeys1)
        val v2 = selector2.select(timeout = 10, selectedEvents = selectKeys2)

        println("v1=$v1, v2=$v2")
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun reattachTest() {
        val port = UdpConnection.randomPort()
        fun sendDataWithDelay(port: Int, delay: Duration) {
            Thread {
                Thread.sleep(delay.inWholeMilliseconds)
                UdpSocketChannel().use { channel ->
                    ByteBuffer.alloc(30).use { buffer ->
                        println("send tmp data")
                        channel.send(buffer, NetworkAddress.Immutable(host = "127.0.0.1", port = port))
                    }
                }
            }.start()
        }

        fun UdpSocketChannel.skip(dataSize: Int) {
            val read = ByteBuffer.alloc(dataSize).use { buffer ->
                this.recv(buffer, null)
            }
            assertEquals(dataSize, read)
        }

        val selectKeys1 = SelectedEvents.create()
        val selector1 = Selector.open()

        val selectKeys2 = SelectedEvents.create()
        val selector2 = Selector.open()

        val server = UdpSocketChannel()
        server.setBlocking(false)
        var k1: Selector.Key? = null
        var k2: Selector.Key? = null
        k1 = selector1.attach(server, Selector.INPUT_READY)
        k2 = selector2.attach(server, Selector.INPUT_READY)
        server.bind(NetworkAddress.Immutable(host = "127.0.0.1", port = port))
        sendDataWithDelay(port = port, delay = 300.milliseconds)
        val vv1 = measureTimedValue { selector1.select(selectedEvents = selectKeys1, timeout = 1000) }
//        val vv2 = measureTimedValue { selector2.select(selectedEvents = selectKeys2, timeout = 1000) }
        println("vv1=$vv1")
//        println("vv2=$vv2")
//        assertEquals(0, selectKeys1.count())
        selectKeys1.forEach {
            println("selectKeys1->$it ${k1 === it.key} ${k2 === it.key}")
        }
        selectKeys2.forEach {
            println("selectKeys2->$it ${k1 === it.key} ${k2 === it.key}")
        }
//        server.skip(30)
    }
}
