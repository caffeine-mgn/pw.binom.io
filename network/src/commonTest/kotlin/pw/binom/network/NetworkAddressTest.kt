package pw.binom.network

import pw.binom.uuid.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class NetworkAddressTest {

    @Test
    fun dataTest() {
        val addr = NetworkAddressOld.Immutable("127.0.0.1", 123)
        assertEquals("127.0.0.1", addr.host)
        assertEquals(123, addr.port)
        assertTrue(addr.equals(addr.toMutable()))

        assertEquals("127.0.0.1", addr.toMutable().host)
        assertEquals(123, addr.toMutable().port)

        assertEquals("127.0.0.1", addr.toMutable().toImmutable().host)
        assertEquals(123, addr.toMutable().toImmutable().port)

        val addr2 = NetworkAddressOld.Mutable()
        addr2.reset("127.0.0.1", port = 123)
        assertEquals("127.0.0.1", addr2.host)
        assertEquals(123, addr2.port)
    }

    @Test
    fun hashCodeEqualsTest() {
        val v = NetworkAddressOld.Mutable().also {
            it.reset(host = "127.0.0.1", port = 123)
        }
        assertTrue(v.hashCode() != 0, "#1")
        assertTrue(v.toImmutable().hashCode() != 0, "#2")
    }

    @Test
    fun unknownHost() {
        try {
            NetworkAddressOld.Immutable(Random.nextUuid().toString(), 9999)
            fail()
        } catch (e: UnknownHostException) {
            // NOP
        }
        try {
            NetworkAddressOld.Mutable().reset(Random.nextUuid().toString(), 9999)
            fail()
        } catch (e: UnknownHostException) {
            // NOP
        }
    }

    @Test
    fun knownHost() {
        NetworkAddressOld.Immutable("google.com", 9999)
        NetworkAddressOld.Mutable().reset("google.com", 9999)
    }
}
