package pw.binom.io.socket

import pw.binom.Environment
import pw.binom.Platform
import pw.binom.platform
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TcpBindTest {
  @Test
  fun bindRandomPortTest() {
    val socket = TcpNetServerSocket()
    assertEquals(BindStatus.OK, socket.bind(InetAddress.resolve("127.0.0.1").withPort(0)))
    assertNotEquals(null, socket.port)
    assertNotEquals(0, socket.port)
  }

  @Test
  fun alreadyBindTest() {
    val socket = TcpNetServerSocket()
    socket.bind(InetAddress.resolve("127.0.0.1").withPort(0))
    assertEquals(BindStatus.ALREADY_BINDED, socket.bind(InetAddress.resolve("127.0.0.1").withPort(socket.port!!)))
  }

  @Test
  fun bindBindedTest() {
    val socket1 = TcpNetServerSocket()
    socket1.bind(InetAddress.resolve("127.0.0.1").withPort(0))
    val socket2 = TcpNetServerSocket()
    assertEquals(
      BindStatus.ADDRESS_ALREADY_IN_USE,
      socket2.bind(InetAddress.resolve("127.0.0.1").withPort(socket1.port!!)),
    )
  }

  @Test
  fun bindUnixSocket() {
    if (Environment.platform == Platform.MINGW_X86 || Environment.platform == Platform.MINGW_X64) {
      return
    }
    val socket = TcpUnixServerSocket()
    assertEquals(BindStatus.OK, socket.bind("test_sock1"))
    assertEquals(BindStatus.ALREADY_BINDED, socket.bind("test_sock1"))
  }
}
