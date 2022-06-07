package pw.binom.crypto

import pw.binom.Utils
import kotlin.test.Test
import kotlin.test.assertEquals

class ECDSASignerTest {

    @Test
    fun signTest() {
        val pair = Utils.getPair()
        val msg = "45ed6a1a-d2da-4c15-9c0e-a1dbcf15c80c".encodeToByteArray()
        val signature = ECDSASigner.sign(msg, pair.private)
        assertEquals(32, signature.r.toByteArray().size)
        assertEquals(32, signature.s.toByteArray().size)
        val data = signature.toByteArray()
        assertEquals(signature, ECDSASignature.create(data))
    }
}
