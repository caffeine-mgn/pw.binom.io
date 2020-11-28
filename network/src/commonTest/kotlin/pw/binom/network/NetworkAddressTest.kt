package pw.binom.network

import pw.binom.uuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.fail

class NetworkAddressTest {

    @Test
    fun unknownHost() {
        try {
            NetworkAddress.Immutable(Random.uuid().toString(), 9999)
            fail()
        } catch (e: UnknownHostException) {
        }
        try {
            NetworkAddress.Mutable().reset(Random.uuid().toString(), 9999)
            fail()
        } catch (e: UnknownHostException) {
        }
    }

    @Test
    fun knownHost() {
        NetworkAddress.Immutable("google.com", 9999)
        NetworkAddress.Mutable().reset("google.com", 9999)
    }
}