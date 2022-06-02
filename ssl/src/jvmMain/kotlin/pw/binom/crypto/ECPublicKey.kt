package pw.binom.crypto

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import pw.binom.BouncycastleUtils
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec

actual class ECPublicKey(val native: BCECPublicKey) : Key.Public {
    override val algorithm: KeyAlgorithm
        get() = KeyAlgorithm.ECDSA
    override val data: ByteArray
        get() = native.encoded
    override val format: String
        get() = "X.509"
    actual val q: EcPoint by lazy {
        EcPoint(native.q, ECCurve(native.q.curve))
    }

    init {
        println("public format: ${native.format}")
    }

    actual companion object {
        actual fun load(data: ByteArray): ECPublicKey {
            BouncycastleUtils.check()
            val c = KeyFactory.getInstance("EC", BouncycastleUtils.provider)
            val key = c.generatePublic(X509EncodedKeySpec(data))
            return ECPublicKey(key as BCECPublicKey)
        }
    }
}
