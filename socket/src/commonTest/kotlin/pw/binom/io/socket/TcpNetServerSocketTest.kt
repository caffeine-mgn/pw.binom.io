package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import pw.binom.io.use
import pw.binom.io.wrap
import pw.binom.testing.shouldContentEquals
import pw.binom.testing.shouldEquals
import pw.binom.testing.shouldNotNull
import kotlin.random.Random
import kotlin.test.Test

class TcpNetServerSocketTest {
  @Test
  fun blockingTest() {
    val server = TcpNetServerSocket()
    server.bind()
    server.port.shouldNotNull()
    val client = TcpClientNetSocket()
    client.connect(server) shouldEquals ConnectStatus.OK
    val e = MutableInetAddress()
    val newClient = server.accept(e).shouldNotNull()
    val data = Random.nextBytes(100)
    data.wrap { p ->
      client.send(p) shouldEquals data.size
    }
    ByteBuffer(200).use { buf ->
      newClient.receive(buf) shouldEquals data.size
      buf.flip()
      buf.toByteArray() shouldContentEquals data
    }
  }
}
