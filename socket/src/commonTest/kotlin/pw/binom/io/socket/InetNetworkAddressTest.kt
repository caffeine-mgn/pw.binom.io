package pw.binom.io.socket

import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InetNetworkAddressTest {

  @Test
  fun testUnknownHost() {
    assertNull(InetAddress.resolveOrNull(Random.nextUuid().toString()))
    assertTrue(InetAddress.resolveAll(Random.nextUuid().toString()).isEmpty())
  }

  @Test
  fun resolveSuccess() {
    val list = InetAddress.resolveAll("localhost")
    assertEquals(2, list.size)
    assertTrue(list.any { it.host == "127.0.0.1" })
    assertTrue(list.any { it.host == "0:0:0:0:0:0:0:1" || it.host == "::1" })
  }
}
