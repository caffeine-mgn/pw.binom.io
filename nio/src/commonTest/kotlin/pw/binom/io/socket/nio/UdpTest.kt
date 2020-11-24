package pw.binom.io.socket.nio

import pw.binom.ByteBuffer
import pw.binom.async
import pw.binom.io.socket.MutableNetworkAddress
import pw.binom.io.socket.NetworkAddress
import pw.binom.io.socket.create
import pw.binom.uuid
import pw.binom.wrap
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Test
import kotlin.test.assertEquals

class UdpTest {

    @Test
    fun test() {
        val port = Random.nextInt(9999 until Short.MAX_VALUE)
        val manager = SocketNIOManager()
        val server = manager.bindUDP(NetworkAddress.create(port))

        val client = manager.openUdp()
        var done = false
        var exception: Throwable? = null
        val request = Random.uuid().toString()
        val response = Random.uuid().toString()
        async {
            try {
                println("#1 Send")
                client.write(
                    ByteBuffer.wrap(request.encodeToByteArray()),
                    NetworkAddress.create("127.0.0.1", port)
                )

                val buf = ByteBuffer.alloc(512)
                val addr = MutableNetworkAddress()
                println("#2 Read")
                server.read(buf, addr)
                buf.flip()
                assertEquals(request, buf.toByteArray().decodeToString())

                println("#3 Send")
                server.write(ByteBuffer.wrap(response.encodeToByteArray()), addr)
                buf.clear()

                println("#4 Read")
                client.read(buf, addr)
                buf.flip()
                assertEquals(response, buf.toByteArray().decodeToString())
            } catch (e: Throwable) {
                e.printStackTrace()
                exception = e
            } finally {
                done = true
            }
        }
        while (!done) {
            manager.update()
        }
        server.close()
        exception?.let { throw it }
    }
}