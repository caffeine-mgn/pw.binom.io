package pw.binom.io.socket

import kotlin.test.Test

class NetworkInterfacesTest {
  @Test
  fun getList() {
    val list = NetworkInterface.getAvailable()
    list.forEach {
      println("->$it")
    }
  }
}
