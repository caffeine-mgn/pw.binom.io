package pw.binom.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import pw.binom.*
import pw.binom.io.bufferedReader
import pw.binom.io.readText
import pw.binom.wrap
import kotlin.test.Test
import kotlin.test.assertEquals

class UdpTest {
    @Test
    fun test() = runTest {
        val port = UdpConnection.randomPort()
        println("port=$port")
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
