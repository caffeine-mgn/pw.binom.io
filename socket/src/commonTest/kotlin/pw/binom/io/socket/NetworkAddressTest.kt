package pw.binom.io.socket

import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkAddressTest {
  @Test
  fun portTest() {
    InetNetworkAddress.create(host = "2001:0db8:85a3:0000:0000:8a2e:0370:7334", port = 33)
    val host = "127.0.0.1"
    val port = 8899
    val address = InetNetworkAddress.create(host = host, port = port)
    assertEquals(host, address.host)
    assertEquals(port, address.port)
  }
}
