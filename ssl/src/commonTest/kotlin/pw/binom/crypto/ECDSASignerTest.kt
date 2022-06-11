package pw.binom.crypto

import pw.binom.Utils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ECDSASignerTest {

    @Test
    fun verifyTest() {
        val data = "Hello World".encodeToByteArray()
        val pair = Utils.getPair()
        val signer = ECDSASigner(pair.private)
        val signature = signer.sign(data)
        val signer2 = ECDSASigner(pair.public)
        assertTrue(signer2.verify(data, signature))
    }

    @Test
    fun signTest() {
        val pair = Utils.getPair()
        val msg = "45ed6a1a-d2da-4c15-9c0e-a1dbcf15c80c".encodeToByteArray()
        val signer = ECDSASigner(pair.private)
        val signature = signer.sign(msg)
        assertEquals(32, signature.r.toByteArray().size)
        assertEquals(32, signature.s.toByteArray().size)
        val data = signature.toByteArray()
        assertEquals(signature, ECDSASignature.create(data))
    }
}
