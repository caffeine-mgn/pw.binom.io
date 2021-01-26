package pw.binom.network

import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SelectorTest {

    @Test
    fun selectTimeoutTest1() {
        val selector = Selector.open()
        assertEquals(0, selector.select(1000) { _, _ -> })
    }

    @Ignore
    @Test
    fun selectTimeoutTest2() {
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        selector.attach(client)
        assertEquals(0, selector.select(1000) { _, _ -> })
    }

    @Test
    fun connectTest2() {
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        val key = selector.attach(client)
        key.listensFlag=0
        client.connect(NetworkAddress.Immutable("google.com", 443))

        assertEquals(0, selector.select(5000) { key, mode ->

        })
    }

    @Test
    fun connectTest() {
        val selector = Selector.open()
        val client = TcpClientSocketChannel()
        val key = selector.attach(client)
        println("try set flags...  ${Selector.EVENT_CONNECTED}")
        key.listensFlag=Selector.EVENT_CONNECTED
        println("Flag setted")
        client.connect(NetworkAddress.Immutable("google.com", 443))

        assertEquals(1, selector.select(5000) { key, mode ->
            assertTrue(mode and Selector.EVENT_CONNECTED != 0)
            assertTrue(mode and Selector.OUTPUT_READY != 0)
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
            if (mode and Selector.INPUT_READY!=0) {
                println("Selector.INPUT_READY")
            }
            if (mode and Selector.OUTPUT_READY!=0) {
                println("Selector.OUTPUT_READY")
            }
            if (mode and Selector.EVENT_CONNECTED!=0) {
                println("Selector.EVENT_CONNECTED")
            }
            if (mode and Selector.EVENT_ERROR!=0) {
                println("Selector.EVENT_ERROR")
            }
            assertTrue(mode and Selector.EVENT_ERROR != 0)
            client.close()
        }
        assertEquals(0, selector.select(1000) { _, _ -> })
    }

    @Test
    fun bindTest(){
        val selector = Selector.open()
        val server = TcpServerSocketChannel()
        val addr = NetworkAddress.Immutable("0.0.0.0", Random.nextInt(1000, 5999))
        server.bind(addr)
        selector.attach(server)
        assertEquals(0, selector.select(1000) { _, _ -> })
    }
}