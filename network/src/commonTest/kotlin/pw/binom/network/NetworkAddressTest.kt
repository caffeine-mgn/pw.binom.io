package pw.binom.network

import pw.binom.nextUuid
import kotlin.random.Random
import kotlin.test.*

class NetworkAddressTest {

    @Test
    fun dataTest() {
        val addr = NetworkAddress.Immutable("127.0.0.1", 123)
        assertEquals("127.0.0.1", addr.host)
        assertEquals(123, addr.port)

        assertEquals("127.0.0.1", addr.toMutable().host)
        assertEquals(123, addr.toMutable().port)

        assertEquals("127.0.0.1", addr.toMutable().toImmutable().host)
        assertEquals(123, addr.toMutable().toImmutable().port)

        val addr2 = NetworkAddress.Mutable()
        addr2.reset("127.0.0.1",123)
        assertEquals("127.0.0.1", addr2.host)
        assertEquals(123, addr2.port)
    }

    @Test
    fun hashCodeEqualsTest() {
        val v = NetworkAddress.Mutable().also {
            it.reset(host = "127.0.0.1", port = 123)
        }
        assertTrue(v.hashCode() != 0, "#1")
        assertTrue(v.toImmutable().hashCode() != 0, "#2")
    }

    @Test
    fun unknownHost() {
        try {
            NetworkAddress.Immutable(Random.nextUuid().toString(), 9999)
            fail()
        } catch (e: UnknownHostException) {
            //NOP
        }
        try {
            NetworkAddress.Mutable().reset(Random.nextUuid().toString(), 9999)
            fail()
        } catch (e: UnknownHostException) {
            //NOP
        }
    }

    @Test
    fun knownHost() {
        NetworkAddress.Immutable("google.com", 9999)
        NetworkAddress.Mutable().reset("google.com", 9999)
    }
}