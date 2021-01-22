package pw.binom.dns

import pw.binom.dns.protocol.fromDns
import pw.binom.dns.protocol.toDnsString
import kotlin.test.Test
import kotlin.test.assertEquals

class DnsUtilsTest {

    @Test
    fun stringToDns() {
        assertEquals("\u0003www\u0006google\u0003com", "www.google.com".toDnsString().concatToString())
    }

    @Test
    fun dnsToString() {
        assertEquals("www.google.com", "\u0003www\u0006google\u0003com".fromDns())
    }
}