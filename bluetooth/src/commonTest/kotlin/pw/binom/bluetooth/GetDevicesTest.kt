package pw.binom.bluetooth

import pw.binom.io.use
import pw.binom.thread.Thread
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class GetDevicesTest {
  val selfDeviceAddress1 = "00:1A:7D:DA:71:11" // subochev-work
  val selfDeviceAddress2 = "8A:88:4B:21:20:34" // subochev-work #1
  val workBookAddress = "0C:9A:3C:EA:4C:09"

  @Test
  fun dd() {
    val e = Devices.getDevices()
    val workBookAddress = Address.parse(workBookAddress)
    e.last().let { device1 ->
      device1.open().use { device ->
        println("Getting SPD from $workBookAddress using ${device1.address}")
//        val devices = device1.discover(5.seconds)
        device.spdRequest(workBookAddress)
//        println("->$devices")
      }
    }
    println(e)
  }

  @Test
  fun spdTest() {
    val e = Devices.getDevices()
    val self = e.find { it.address.toString() == selfDeviceAddress2 }!!
    println("--->$self")
    val spdRequest = ubyteArrayOf(
      0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, 0x00u, // Заголовок
      0x35u, 0x03u, 0x19u, 0x01u, 0x00u, 0xFFu, 0xFFu // SDP-запрос
    ).toByteArray()
    self.open().use { dev ->
      dev.openL2CAP(Address.parse(workBookAddress), PSM.SPD).use { socket ->
        socket.writeFully(spdRequest)
        val buf = ByteArray(1024)
        val len = socket.read(buf)
        println("len=$len")
      }
    }
  }

  @Test
  fun bb() {
    val e = Devices.getDevices()
    val self = e.find { it.address.toString() == selfDeviceAddress2 }!!
    self.open().use {
      it.publishSPP().use {
        it.accept()
      }
    }
    println("--->$self")
    self.open().use {
      println("found:\n${it.discover(5.seconds).joinToString("\n")}")
    }
  }

  //  @Test
  fun aa() {
    val e = Devices.getDevices()
    val self = e.find { it.address.toString() == selfDeviceAddress2 }!!
    self.open().use { openned ->
//      println("discover for ${self.name} ${self.address}")
//      val result = it.discover()
//      println("Found ${result.size}:\n${result.joinToString("\n")}")

      println("Подключаемся!")
      openned.openSPP(
        remoteAddress = Address.parse(workBookAddress),
        channel = 4,
      ).use { sppConnection ->
        Thread.sleep(5.seconds)
      }
      println("Отключились!")
    }
    println("\n\n\n")
    println(e)
  }
}
