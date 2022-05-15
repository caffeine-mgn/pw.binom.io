package pw.binom.security

import pw.binom.Utils
import pw.binom.io.socket.ssl.toHex
import pw.binom.ssl.KeyAlgorithm
import kotlin.test.Test
import kotlin.test.assertTrue

class SignatureTest {

    private val someData = "Hello world".encodeToByteArray()

    @Test
    fun testRsa() {
        val pair = Utils.findPair("rsa", KeyAlgorithm.RSA)
        val signature = Signature.getInstance("SHA1withRSA")
        signature.init(pair.private)
        signature.update(someData)
        val signed = signature.sign()
        println("signature: ${signed.toHex()}")
        val signature2 = Signature.getInstance("SHA1withRSA")
        signature2.init(pair.public)
        signature2.update(someData)
        val result = signature2.verify(signed)
        assertTrue(result)
    }

    @Test
    fun testEcdsa() {
        val pair = Utils.findPair("ec", KeyAlgorithm.ECDSA)
        val signature = Signature.getInstance("SHA1withECDSA")
        signature.init(pair.private)
        signature.update(someData)
        val signed = signature.sign()
        println("signature: ${signed.toHex()}")
        val signature2 = Signature.getInstance("SHA1withECDSA")
        signature2.init(pair.public)
        signature2.update(someData)
        val result = signature2.verify(signed)
        assertTrue(result)
    }
}
