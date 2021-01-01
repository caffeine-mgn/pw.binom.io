package pw.binom.dns

import kotlin.test.Test
import kotlin.test.assertEquals

class DnsUtilsTest {

    @Test
    fun stringToDns() {
        assertEquals("\u0003www\u0006google\u0003com", "www.google.com".toDnsString())
    }

    @Test
    fun dnsToString() {
        assertEquals("www.google.com", "\u0003www\u0006google\u0003com".fromDns())
    }
}