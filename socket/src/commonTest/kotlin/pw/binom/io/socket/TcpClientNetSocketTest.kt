package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import pw.binom.io.use
import pw.binom.testing.Testing
import pw.binom.wrap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TcpClientNetSocketTest {
  val requestBytes = ("GET / HTTP/1.0\r\n" +
    "Host: 127.0.0.1:7143\r\n" +
    "\r\n").encodeToByteArray()

  @Test
  fun connectBlockingTest() {
    val socket = TcpClientNetSocket()
    val host = InetAddress.resolveOrNull("127.0.0.1")!!.withPort(7143)
    val connectResult = socket.connect(host)
    assertEquals(ConnectStatus.OK, connectResult)
    ByteBuffer.wrap(requestBytes).use { data ->
      assertEquals(requestBytes.size, socket.send(data))
    }
    val txt = ByteBuffer(1024 * 8).use { buffer ->
      assertEquals(376, socket.receive(buffer))
      buffer.flip()
      buffer.toByteArray().decodeToString()
    }
  }

  @Test
  fun connectNonBlockingTest() =Testing.sync{
    val selector = Selector()
    val socket = TcpClientNetSocket()
    socket.blocking = false
    val attachKey = selector.attach(socket)
    val host = InetAddress.resolveOrNull("127.0.0.1")!!.withPort(7143)
    attachKey.updateListenFlags(ListenFlags.WRITE + ListenFlags.ONCE)
    assertEquals(ConnectStatus.IN_PROGRESS, socket.connect(host))

    var count = 0
    selector.select(Duration.INFINITE) { event ->
      count++
      assertEquals(attachKey, event.key)
    }
    assertEquals(1, count)
    count = 0
    selector.select(2.seconds) { event ->
      count++
    }
    assertEquals(0, count)
  }
}
