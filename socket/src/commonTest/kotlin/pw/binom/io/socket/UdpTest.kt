package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import pw.binom.io.use
import pw.binom.io.wrap
import pw.binom.testing.shouldEquals
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.Test

class UdpTest {

  @Test
  fun echoBlockingSendReceiveTest() {
    val sock = UdpNetSocket()
    val data = "Hello ${Random.nextUuid()}".encodeToByteArray()
    data.wrap { ptr ->
      sock.send(ptr, InetAddress.resolveOrNull("127.0.0.1")!!.withPort(UDP_ECHO_PORT)) shouldEquals data.size
    }
    val addr = MutableInetSocketAddress()
    ByteBuffer(200).use { ptr ->
      sock.receive(ptr, addr) shouldEquals 67
      ptr.flip()
      ptr.toByteArray()
      addr.port shouldEquals UDP_ECHO_PORT
    }
  }

  @Test
  fun blockingBindTest() {
    val socket1 = UdpNetSocket()
    val socket2 = UdpNetSocket()
    socket1.bind(InetAddress.resolveOrNull("127.0.0.1")!!.withPort(0)) shouldEquals BindStatus.OK

    val data = Random.nextBytes(30)
    data.wrap { buf ->
      socket2.send(buf, InetAddress.resolveOrNull("127.0.0.1")!!.withPort(socket1.port!!))
    }
    ByteBuffer(100).use { buf ->
      val addr = MutableInetSocketAddress()
      val got = socket1.receive(buf, addr)
      println("got=$got from $addr")
    }
  }

  @Test
  fun echoNonBlockingSendReceiveTest() {
    val socket = UdpNetSocket()
    socket.blocking = false
    Selector().use { selector ->
      selector.attach(socket)
    }
  }
}
