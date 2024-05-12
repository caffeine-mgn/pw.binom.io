package pw.binom.upnp

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import pw.binom.io.ByteBuffer
import pw.binom.io.socket.BindStatus
import pw.binom.io.socket.InetSocketAddress
import pw.binom.io.socket.NetworkInterface
import pw.binom.io.socket.UdpNetSocket
import pw.binom.network.NetworkCoroutineDispatcherImpl
import pw.binom.testing.shouldEquals
import pw.binom.upnp.UPnPDiscover.IP
import pw.binom.upnp.UPnPDiscover.PORT
import pw.binom.upnp.source.DeviceListSource
import kotlin.test.Test
import kotlin.time.Duration.Companion.minutes

class LocalUPnPTest {

  private val localhost = NetworkInterface.getByIp("127.0.0.1")

  @Test
  fun aaa() {
    val ubyte: UByte = 255u
    val byte = ubyte.toByte()
    val int = ubyte.toInt()
    val uint = ubyte.toUInt()
    println("ubyte=$ubyte")
    println("byte=$byte")
    println("int=$int")
    println("uint=$uint")
  }

  @Test
  fun localTest() = runTest {
    val nm = NetworkCoroutineDispatcherImpl()
    withContext(nm) {
//      NetworkInterface.getAvailable().forEach {
//        println("${it.name} -> ${it.ip}")
//      }

      val devices = DeviceListSource(
        listOf(
          UPnPDevice.create(
            st = "ololo",
            location = "http://123123123",
          )
        ),
      )

      val publisher = UPnPDevicePublisher.create(
        networkManager = nm,
        networkInterface = localhost,
        source = devices,
      )

//      val v = UdpNetSocket()
//      v.bind(localhost.ip.withPort(0)) shouldEquals BindStatus.OK
//      nm.attach(v).write(ByteBuffer(100), InetSocketAddress.resolve(host = IP, port = PORT))

      val list = UPnPDiscover.discover(
        nm = nm,
        netIf = localhost,
      )
      println("List [${list.size}]:")
      list.forEach {
        println("${localhost.name}->${it.st} ${it.location}")
      }
    }
  }
}
