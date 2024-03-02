package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import pw.binom.io.use
import pw.binom.io.wrap
import kotlin.random.Random
import kotlin.test.Test

class UdpTest {
  @Test
  fun test() {
    val socket1 = Socket.createUdpNetSocket()
    val socket2 = Socket.createUdpNetSocket()
    socket1.bind(InetNetworkAddress.create(host = "127.0.0.1", port = 0))

    val data = Random.nextBytes(30)
    data.wrap { buf ->
      socket2.send(buf, InetNetworkAddress.create(host = "127.0.0.1", port = socket1.port!!))
    }
    ByteBuffer(100).use { buf ->
      val addr = MutableInetNetworkAddress.create()
      val got = socket1.receive(buf, addr)
      println("got=$got from $addr")
    }
  }
}
