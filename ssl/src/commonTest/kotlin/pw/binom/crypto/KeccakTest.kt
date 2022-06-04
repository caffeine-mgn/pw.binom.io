package pw.binom.crypto

import pw.binom.io.socket.ssl.toHex
import kotlin.test.Test
import kotlin.test.assertEquals

class KeccakTest {

    @Test
    fun test256() {
        val d = Keccak256MessageDigest()
        d.init()
        d.update("63fe75c7-1d2d-40f7-af15-fc3ef7a59da9".encodeToByteArray())
        val ee = d.finish()
        assertEquals(ee.toHex(), "0ad2c6415b82125e7b95f29c177c77bd2bd4ccef743feea61b428da6e7706a89")
    }
}
