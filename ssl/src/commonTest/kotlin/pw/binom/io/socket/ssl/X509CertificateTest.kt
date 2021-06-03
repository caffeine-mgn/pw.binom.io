package pw.binom.io.socket.ssl

import pw.binom.base64.Base64
import pw.binom.date.Date
import pw.binom.ssl.*
import kotlin.test.Test

class X509CertificateTest {
    @Test
    fun test() {

        val pairRoot = KeyGenerator.generate(
            KeyAlgorithm.RSA,
            2048
        )


        val pair1 = KeyGenerator.generate(
            KeyAlgorithm.RSA,
            2048
        )
        val private1 = pair1.createPrivateKey()
        val public2 = X509Builder(
            pair = pair1,
            notBefore = Date(),
            notAfter = Date(Date.nowTime + 1000 * 60 * 60),
            serialNumber = 10,
            issuer = "DC=localhost",
            subject = "CN=localhost",
            sign = pairRoot.createPrivateKey()
        ).generate()
        public2.toString()

        val keyPair = KeyGenerator.generate(KeyAlgorithm.RSA, 2048)
        val cer = X509Builder(
            pair = keyPair,
            subject = "CN=example.com",
            notBefore = Date(Date.nowTime - 1000),
            issuer = "DC=example.com",
            notAfter = Date(Date.nowTime + 1000 * 60 * 60 * 24),
            serialNumber = 1L
        ).generate()

        println("->\n${Base64.encode(cer.save())}")
        val bb = X509Certificate.load(public2.save())
    }
}