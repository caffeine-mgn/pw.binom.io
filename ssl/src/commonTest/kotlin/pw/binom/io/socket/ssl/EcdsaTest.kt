package pw.binom.io.socket.ssl

import pw.binom.ssl.Key
import pw.binom.ssl.Nid
import pw.binom.ssl.generateEcdsa
import kotlin.test.Test

class EcdsaTest {
    @Test
    fun test() {
        val pair = Key.generateEcdsa(Nid.secp256r1)
    }
}
