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

class TcpTest {

    @Test
    fun test() {

        val request = Random.uuid().toString()
        val response = Random.uuid().toString()

        val port = Random.nextInt(9999 until Short.MAX_VALUE)
        val manager = SocketNIOManager()
        val server = manager.bind(
            NetworkAddress.create(port)
        ) { connection ->
            connection {
                val buf = ByteBuffer.alloc(512)
                it.read(buf)
                buf.flip()
                assertEquals(request, buf.toByteArray().decodeToString())
                it.write(ByteBuffer.wrap(response.encodeToByteArray()))
            }
        }

        var done = false
        var exception: Throwable? = null

        async {
            try {
                val client = manager.connect(NetworkAddress.Companion.create("127.0.0.1", port))
                client.write(
                    ByteBuffer.wrap(request.encodeToByteArray())
                )
                val buf = ByteBuffer.alloc(512)
                buf.clear()
                client.read(buf)
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