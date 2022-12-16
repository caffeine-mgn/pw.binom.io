package pw.binom.network

import pw.binom.concurrency.SpinLock
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
    @Ignore
    fun wakeupTest() {
        val selector = SelectorOld.open()
        val selectKeys = SelectedEventsOld.create()
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
    @Ignore
    fun selectTimeoutTest1() {
        val selectKeys = SelectedEventsOld.create()
        val selector = SelectorOld.open()
        val it = selector.select(selectKeys, 1000)
        assertEquals(0, it)
        assertFalse(selectKeys.iterator().hasNext())
    }

    @OptIn(ExperimentalTime::class)
    @Test
    @Ignore
    fun selectTimeoutTest2() {
        val selectKeys = SelectedEventsOld.create()
        val selector = SelectorOld.open()
        val client = TcpClientSocketChannel()
        client.setBlocking(false)
        selector.attach(client)
        val beforeSelecting = TimeSource.Monotonic.markNow()
        val count = selector.select(selectKeys, 1000)
        assertEquals(0, count)
        val it = selectKeys.iterator()
        val selectingTime = beforeSelecting.elapsedNow()
        assertTrue(selectingTime > 0.5.seconds && selectingTime < 1.5.seconds)
        assertFalse(it.hasNext())
    }

    @Test
    @Ignore
    fun connectTest2() {
        val selectKeys = SelectedEventsOld.create()
        val selector = SelectorOld.open()
        val client = TcpClientSocketChannel()
        client.setBlocking(false)
        val key = selector.attach(client)
        try {
            key.listensFlag = 0
            client.connect(NetworkAddressOld.Immutable("google.com", 443))
            selector.select(selectKeys, 2000)
            assertFalse(selectKeys.iterator().hasNext())
        } finally {
            key.close()
            client.close()
            selector.close()
        }
    }

    @Test
    @Ignore
    fun connectTest() {
        val selectKeys = SelectedEventsOld.create()
        val selector = SelectorOld.open()
        val client = TcpClientSocketChannel()
        client.setBlocking(false)
        val key = selector.attach(client)
        key.listensFlag = SelectorOld.EVENT_CONNECTED
        client.connect(NetworkAddressOld.Immutable("google.com", 443))
        selector.select(selectKeys, 5000)
        var it = selectKeys.iterator()
        assertTrue(it.hasNext())
        val mode = it.next().also { println("Event: $it") }.mode
        assertTrue(mode and SelectorOld.OUTPUT_READY != 0, "OUTPUT_READY fail")
        assertTrue(mode and SelectorOld.EVENT_CONNECTED != 0, "EVENT_CONNECTED fail")
        selector.select(selectKeys, 1000)
//        selectKeys.iterator().forEach {
//            println("->$it")
//        }
        it = selectKeys.iterator()
        assertFalse(it.hasNext())
    }

    @Test
    @Ignore
    fun connectionRefusedTest() {
        val selectKeys = SelectedEventsOld.create()
        val selector = SelectorOld.open()
        val client = TcpClientSocketChannel()
        client.setBlocking(false)
        selector.attach(client)
        client.connect(NetworkAddressOld.Immutable("127.0.0.1", 12))

        selector.select(selectKeys, 5000)
        selectKeys.forEach {
            val mode = it.mode
            val key = it.key
            assertTrue(mode and SelectorOld.EVENT_ERROR != 0)
            client.close()
        }
        assertEquals(0, selector.select(selectKeys, 1000))
    }

    @Test
    @Ignore
    fun bindTest() {
        val selectKeys = SelectedEventsOld.create()
        val selector = SelectorOld.open()
        val server = TcpServerSocketChannel()
        val addr = NetworkAddressOld.Immutable("0.0.0.0", Random.nextInt(1000, 5999))
        server.bind(addr)
        server.setBlocking(false)
        selector.attach(server)
        assertEquals(0, selector.select(selectKeys, 1000))
    }

    @Test
    @Ignore
    fun testSeveralKeyOfOneSocket() {
        val server = TcpServerSocketChannel()
        server.setBlocking(false)
        server.bind(NetworkAddressOld.Immutable(host = "127.0.0.1", port = 0))
        val selectKeys1 = SelectedEventsOld.create()
        val selector1 = SelectorOld.open()
        println("#1")
        val key1 = selector1.attach(server)
        println("#2")
        key1.listensFlag = SelectorOld.INPUT_READY
        val selectKeys2 = SelectedEventsOld.create()
        val selector2 = SelectorOld.open()
        println("#3")
        val key2 = selector2.attach(server)
        println("#4")
        key2.listensFlag = SelectorOld.INPUT_READY
        TcpClientSocketChannel().connect(NetworkAddressOld.Immutable(host = "127.0.0.1", port = server.port!!))

        val v1 = selector1.select(selectedEvents = selectKeys1, timeout = 10)
        val v2 = selector2.select(selectedEvents = selectKeys2, timeout = 10)

        println("v1=$v1, v2=$v2")
    }

    @OptIn(ExperimentalTime::class)
    @Test
    @Ignore
    fun exclusiveTest() {
//        val rr = ReentrantLock()
//        val con = rr.newCondition()
// EPOLLET
        val selector = SelectorOld.open()

        class NThread : ThreadWorker() {

            val list = SelectedEventsOld.create(100)
            private val lock = SpinLock()
            var lastSelectCount = 0
                private set

            fun trySelect() {
                dispatch {
                    println("Selecting...")
                    val count = measureTimedValue { selector.select(selectedEvents = list, timeout = 1000) }
                    lastSelectCount = count.value
                    println("select count: $count")
                }
            }

            override fun beforeStop() {
                list.close()
                super.beforeStop()
            }
        }

        val s1 = NThread()
        val s2 = NThread()

        val server = UdpSocketChannel()
        server.setBlocking(false)
        server.bind(NetworkAddressOld.Immutable(host = "0.0.0.0", port = 0))
        val addr = NetworkAddressOld.Immutable(host = "127.0.0.1", port = server.port!!)
        val s1Key = selector.attach(server, mode = SelectorOld.INPUT_READY)
//        val s2Key = selector.attach(server, mode = Selector.INPUT_READY)

        s1.trySelect()
        s2.trySelect()

        val client = UdpSocketChannel()
        fun sendNow() {
            ByteBuffer.alloc(16).use { buffer ->
                client.send(buffer, addr)
            }
        }
        sendNow()
        Thread.sleep(3_000)
        val threads = listOf(s1, s2)
        assertEquals(1, threads.count { it.lastSelectCount == 0 })
        assertEquals(1, threads.count { it.lastSelectCount == 1 })
    }

    private fun sendUdp(port: Int?) = sendUdp(NetworkAddressOld.Immutable(host = "127.0.0.1", port = port!!))

    fun sendUdp(address: NetworkAddressOld) {
        UdpSocketChannel().use { c ->
            c.setBlocking(true)
            ByteBuffer.alloc(42).use { buffer ->
                c.send(buffer, address)
            }
        }
    }

    @Test
    @Ignore
    fun resetListenKeysAfterSelect() {
        val selectKeys1 = SelectedEventsOld.create()
        val selector1 = SelectorOld.open()
        val b = UdpSocketChannel()
        b.setBlocking(false)
        b.bind(NetworkAddressOld.Immutable("127.0.0.1", port = 0))
        val udpChannel = selector1.attach(b, mode = SelectorOld.INPUT_READY)
        sendUdp(b.port)
        selector1.select(selectedEvents = selectKeys1)
        val event = selectKeys1.single()
        assertTrue(event.mode or SelectorOld.INPUT_READY > 0)
        assertEquals(0, udpChannel.listensFlag)
    }

    @Test
    @Ignore
    fun testReselect() {
        val selectKeys1 = SelectedEventsOld.create()
        val selector1 = SelectorOld.open()
        val b = UdpSocketChannel()
        b.setBlocking(false)
        b.bind(NetworkAddressOld.Immutable("127.0.0.1", port = 0))
        val udpChannel = selector1.attach(b, mode = SelectorOld.INPUT_READY)

        val c = UdpSocketChannel()
        c.setBlocking(true)
        ByteBuffer.alloc(42).use { buffer ->
            c.send(buffer, NetworkAddressOld.Immutable(host = "127.0.0.1", port = b.port!!))
        }
        selector1.select(selectedEvents = selectKeys1, timeout = 1_000)
        assertEquals(1, selectKeys1.count())
        selector1.select(selectedEvents = selectKeys1, timeout = 1_000)
        assertEquals(0, selectKeys1.count())
    }

    @Deprecated(message = "")
    @Ignore
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
                        channel.send(buffer, NetworkAddressOld.Immutable(host = "127.0.0.1", port = port))
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

        val selectKeys1 = SelectedEventsOld.create()
        val selector1 = SelectorOld.open()

        val selectKeys2 = SelectedEventsOld.create()
        val selector2 = SelectorOld.open()

        val server = UdpSocketChannel()
        server.setBlocking(false)
        var k1: SelectorOld.Key? = null
        var k2: SelectorOld.Key? = null
        k1 = selector1.attach(server, mode = SelectorOld.INPUT_READY)
        k2 = selector2.attach(server, mode = SelectorOld.INPUT_READY)
        server.bind(NetworkAddressOld.Immutable(host = "127.0.0.1", port = port))
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
