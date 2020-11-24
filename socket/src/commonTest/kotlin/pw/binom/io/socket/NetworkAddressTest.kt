package pw.binom.io.socket

import kotlin.test.Test
import kotlin.test.assertEquals

class NetworkAddressTest{
    @Test
    fun v4Test() {
        val bb = NetworkAddress.create("127.0.0.1", 80)
        assertEquals(NetworkAddress.Type.IPV4, bb.type)
        assertEquals(80, bb.port)
        assertEquals("127.0.0.1", bb.host)
    }

    @Test
    fun v6Test() {
        val bb = NetworkAddress.create("2001:db8:85a3:0:0:8a2e:370:7334", 80)
        assertEquals(NetworkAddress.Type.IPV6, bb.type)
        assertEquals(80, bb.port)
        assertEquals("2001:db8:85a3:0:0:8a2e:370:7334", bb.host)
    }
}