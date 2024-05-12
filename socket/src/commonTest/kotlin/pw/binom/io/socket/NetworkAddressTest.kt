package pw.binom.io.socket

import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkAddressTest {
  @Test
  fun portTest() {
    val host = "127.0.0.1"
    val port = 8899
    val address = DomainNetworkAddress(host).resolve()!!.withPort(port)
    assertEquals(host, address.host)
    assertEquals(port, address.port)
  }
}
