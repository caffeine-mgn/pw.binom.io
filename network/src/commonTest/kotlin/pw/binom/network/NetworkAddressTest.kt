package pw.binom.network

import pw.binom.nextUuid
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.fail

class NetworkAddressTest {

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