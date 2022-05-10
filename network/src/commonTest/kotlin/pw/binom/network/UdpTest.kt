package pw.binom.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import pw.binom.DEFAULT_BUFFER_SIZE
import pw.binom.io.ByteBuffer
import pw.binom.io.ClosedException
import pw.binom.io.bufferedReader
import pw.binom.io.readText
import pw.binom.wrap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class UdpTest {

    @Test
    fun randomPortTest() {
        assertTrue(UdpConnection.randomPort() > 0)
    }

    @Test
    fun closeByKey() {
        val channel = UdpSocketChannel()
        val selector = Selector.open()
        channel.setBlocking(false)
        val key = selector.attach(channel, 0, null)
        channel.close()
        try {
            key.close()
            fail()
        } catch (e: ClosedException) {
            // Do nothing
        }
    }

    @Test
    fun testipv6() = runTest(dispatchTimeoutMs = 5_000) {
        val udp6 = Dispatchers.Network.attach(UdpSocketChannel())
        val udp4 = Dispatchers.Network.attach(UdpSocketChannel())
        val udpClient = Dispatchers.Network.attach(UdpSocketChannel())
        udp6.bind(NetworkAddress.Immutable(host = "0:0:0:0:0:0:0:1", port = 0))
        udp4.bind(NetworkAddress.Immutable(host = "127.0.0.1", port = 0))
        println("#1 udp6.port=${udp6.port}")
        val v6 = NetworkAddress.Immutable(host = "0:0:0:0:0:0:0:1", port = udp6.port!!)
        println("#2")
        val ya6 = NetworkAddress.Immutable(host = "2a02:6b8:0:0:0:0:2:242", port = 8080)
        println("#3 udp4.port=${udp4.port}")
        val v4 = NetworkAddress.Immutable(host = "127.0.0.1", port = udp4.port!!)
        println("#4")

        println("v6 server: ${udp6.port!!} ya6: $ya6")
        println("---===SEND TO 6===---")
        udpClient.write("hello", v6)
        val p = NetworkAddress.Mutable()
        var txt = udp6.read(p)
        println("v6 txt: $txt, address: ${p.toImmutable()}")

        println("---===SEND TO 4===---")
//        udpClient.write("hello", v4As6)
        udpClient.write("hello", v4)
        println("reading v4...")
        txt = udp4.read(p)
        println("v4 txt: $txt, address: ${p.toImmutable()}")

        udp6.close()
    }

    suspend fun UdpConnection.write(text: String, address: NetworkAddress) =
        text.encodeToByteArray().wrap { data ->
            write(data = data, address = address)
        }

    suspend fun UdpConnection.read(address: NetworkAddress.Mutable?) =
        ByteBuffer.alloc(DEFAULT_BUFFER_SIZE) { data ->
            read(data, address = address)
            data.flip()
            data.toByteArray().decodeToString()
        }

    @Test
    fun test() = runTest {
        val port = UdpConnection.randomPort()
        val server = Dispatchers.Network.bindUdp(NetworkAddress.Immutable(port = port))
        val client = Dispatchers.Network.attach(UdpSocketChannel())
        val message = "Hello"
        message.encodeToByteArray().wrap {
            client.write(it, NetworkAddress.Immutable(host = "127.0.0.1", port = port))
        }
        val resp = ByteBuffer.alloc(message.length * 2) {
            server.read(it, null)
            it.flip()
            it.bufferedReader().readText()
        }
        assertEquals(message, resp)
    }
}
