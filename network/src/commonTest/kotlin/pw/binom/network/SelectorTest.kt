package pw.binom.network

import kotlin.random.Random
import kotlin.test.*

class SelectorTest {

    @Test
    fun selectTimeoutTest1() {
        val selector = Selector.open()
        val it = selector.select(1000)
        assertFalse(it.hasNext())
    }

    @Test
    fun selectTimeoutTest2() {
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        selector.attach(client)
        val it = selector.select(1000)
        it.forEach {
            println("->$it")
        }
        assertFalse(it.hasNext())
    }

    @Test
    fun connectTest2() {
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        val key = selector.attach(client)
        try {
            key.listensFlag = 0
            client.connect(NetworkAddress.Immutable("google.com", 443))
            assertFalse(selector.select(2000).hasNext())
        } finally {
            key.close()
            client.close()
            selector.close()
        }
    }

    @Test
    fun connectTest() {
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        val key = selector.attach(client)
        println("try set flags...  ${Selector.EVENT_CONNECTED}")
        key.listensFlag = Selector.EVENT_CONNECTED
        println("Flag setted")
        client.connect(NetworkAddress.Immutable("google.com", 443))
        var it = selector.select(5000)
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
        it = selector.select(1000)
        assertFalse(it.hasNext())
    }

    @Test
    fun connectionRefusedTest() {
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        selector.attach(client)
        client.connect(NetworkAddress.Immutable("127.0.0.1", 12))

        selector.select(5000) { key, mode ->
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
        assertEquals(0, selector.select(1000) { _, _ -> })
    }

    @Test
    fun bindTest() {
        val selector = Selector.open()
        val server = TcpServerSocketChannel()
        val addr = NetworkAddress.Immutable("0.0.0.0", Random.nextInt(1000, 5999))
        server.bind(addr)
        selector.attach(server)
        assertEquals(0, selector.select(1000) { _, _ -> })
    }
}