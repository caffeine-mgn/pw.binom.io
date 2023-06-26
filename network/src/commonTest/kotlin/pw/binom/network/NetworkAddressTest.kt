package pw.binom.network

import pw.binom.io.socket.MutableInetNetworkAddress
import pw.binom.io.socket.InetNetworkAddress
import pw.binom.io.socket.UnknownHostException
import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class NetworkAddressTest {

    @Test
    fun dataTest() {
        val addr = InetNetworkAddress.create("127.0.0.1", 123)
        assertEquals("127.0.0.1", addr.host)
        assertEquals(123, addr.port)
        assertTrue(addr.equals(addr.toMutable()))

        assertEquals("127.0.0.1", addr.toMutable().host)
        assertEquals(123, addr.toMutable().port)

        assertEquals("127.0.0.1", addr.toMutable().toImmutable().host)
        assertEquals(123, addr.toMutable().toImmutable().port)

        val addr2 = MutableInetNetworkAddress.create()
        addr2.update(host = "127.0.0.1", port = 123)
        assertEquals("127.0.0.1", addr2.host)
        assertEquals(123, addr2.port)
    }

    @Test
    fun hashCodeEqualsTest() {
        val v = MutableInetNetworkAddress.create().also {
            it.update(host = "127.0.0.1", port = 123)
        }
        assertTrue(v.hashCode() != 0, "#1")
        assertTrue(v.toImmutable().hashCode() != 0, "#2")
    }

    @Test
    fun unknownHost() {
        try {
            InetNetworkAddress.create(host = Random.nextUuid().toString(), port = 9999)
            fail()
        } catch (e: UnknownHostException) {
            // Do nothing
        }
    }

    @Test
    fun knownHost() {
        InetNetworkAddress.create(host = "google.com", port = 9999)
        MutableInetNetworkAddress.create(host = "google.com", port = 9999)
    }
}
