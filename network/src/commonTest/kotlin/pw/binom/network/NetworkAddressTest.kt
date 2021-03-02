package pw.binom.network

import pw.binom.uuid
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.fail

class NetworkAddressTest {

    @Ignore
    @Test
    fun unknownHost() {
        try {
            NetworkAddress.Immutable(Random.uuid().toString(), 9999)
            fail()
        } catch (e: UnknownHostException) {
            //NOP
        }
        try {
            NetworkAddress.Mutable().reset(Random.uuid().toString(), 9999)
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