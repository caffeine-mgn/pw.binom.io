package pw.binom.ssl

import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec


class PrivateKeyImpl(override val algorithm: KeyAlgorithm, override val native: java.security.PrivateKey) : pw.binom.ssl.PrivateKey {
    override val data: ByteArray
        get() = native.encoded

    override fun close() {
        //NOPs
    }

}

actual fun PrivateKey.Companion.loadRSA(data: ByteArray): pw.binom.ssl.PrivateKey {
    val o = ByteArrayInputStream(data)
    val r = org.bouncycastle.util.io.pem.PemReader(InputStreamReader(o))
    val oo = r.readPemObject()
    val kf = KeyFactory.getInstance("RSA")
    val vv = kf.generatePrivate(PKCS8EncodedKeySpec(oo.content))
    return PrivateKeyImpl(algorithm = KeyAlgorithm.RSA, native = vv)
}