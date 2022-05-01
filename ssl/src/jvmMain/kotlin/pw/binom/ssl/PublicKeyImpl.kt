package pw.binom.ssl

import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec

class PublicKeyImpl(
    override val algorithm: KeyAlgorithm,
    override val native: java.security.PublicKey,
) : PublicKey {
    override val data: ByteArray
        get() = native.encoded

    override fun close() {
        // NOP
    }
}

actual fun PublicKey.Companion.loadRSA(data: ByteArray): PublicKey {
    val o = ByteArrayInputStream(data)
    val r = org.bouncycastle.util.io.pem.PemReader(InputStreamReader(o))
    val oo = r.readPemObject()
    val kf = KeyFactory.getInstance("RSA")
    val vv = kf.generatePublic(PKCS8EncodedKeySpec(oo.content))
    return PublicKeyImpl(algorithm = KeyAlgorithm.RSA, native = vv)
}
