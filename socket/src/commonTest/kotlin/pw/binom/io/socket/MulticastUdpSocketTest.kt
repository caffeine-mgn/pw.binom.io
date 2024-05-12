package pw.binom.io.socket

import pw.binom.io.ByteBuffer
import pw.binom.io.use
import pw.binom.io.wrap
import pw.binom.testing.shouldContentEquals
import pw.binom.testing.shouldEquals
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class MulticastUdpSocketTest {
  companion object {
    const val IP = "239.255.255.250"
    const val PORT = 4321
  }

  private val localInterface = NetworkInterface.getByIp("127.0.0.1")

  @Test
  fun blockingReceiveTest() {
    val udp = MulticastUdpSocket(
      networkInterface = localInterface,
      port = PORT,
    )
    udp.setTtl(UByte.MAX_VALUE)
    val addr = MutableInetSocketAddress()
    println("localInterface: ${localInterface.ip}")
    udp.joinGroup(InetAddress.resolve(IP).withPort(PORT), localInterface)
    ByteBuffer(100).use { data ->
      udp.receive(data, addr) shouldEquals 17
      data.flip()
      data.toByteArray().decodeToString() shouldEquals "Some Data on 4321"
    }
  }

  private fun sendStubUdp(port: Int): ByteArray {
    val sendUdp = MulticastUdpSocket(
      networkInterface = localInterface,
      port = port,
    )
    val data = Random.nextUuid().toString().encodeToByteArray()
    data.wrap { buf ->
      sendUdp.send(buf, InetAddress.resolve(IP).withPort(port)) shouldEquals data.size
    }
    return data
  }

  @Test
  fun blockingSendTest() {
    val port = 1945

    val udp = MulticastUdpSocket(
      networkInterface = localInterface,
      port = port,
    )
    udp.setTtl(UByte.MAX_VALUE)
    udp.joinGroup(InetAddress.resolve(IP).withPort(port), localInterface)

    val data = sendStubUdp(port)

    ByteBuffer(100).use { dest ->
      udp.receive(dest, null) shouldEquals data.size
      dest.flip()
      dest.toByteArray() shouldContentEquals data
    }
  }

  @Test
  fun nonBlockingReceive() {
    val port = 1944

    val udp = MulticastUdpSocket(
      networkInterface = localInterface,
      port = port,
    )
    udp.setTtl(UByte.MAX_VALUE)
    udp.joinGroup(InetAddress.resolve(IP).withPort(port), localInterface)
    udp.blocking = false

    Selector().use { selector ->
      val key = selector.attach(udp)
      key.updateListenFlags(ListenFlags.READ)
      var count = 0
      selector.select(2.seconds) {
        count++
      }
      ByteBuffer(100).use { buf ->
        udp.receive(buf, null)
      } shouldEquals 0

      count shouldEquals 0
      val data = sendStubUdp(port)
      selector.select(2.seconds) {
        count++
      }
      count shouldEquals 1
      ByteBuffer(100).use { buf ->
        udp.receive(buf, null) shouldEquals data.size
        buf.flip()
        buf.toByteArray() shouldContentEquals data
      }
    }
  }
}
