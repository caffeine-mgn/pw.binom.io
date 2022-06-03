package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import pw.binom.BouncycastleUtils
import pw.binom.ssl.ECKey
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm
import java.security.KeyFactory

actual class ECPrivateKey(val native: BCECPrivateKey) : Key.Private, ECKey {
    override val algorithm: KeyAlgorithm
        get() = KeyAlgorithm.ECDSA
    override val data: ByteArray
        get() = native.encoded
    override val format: String
        get() = "PKCS#8"

    actual val d: BigInteger by lazy {
        native.d.toBigInteger()
    }

    actual companion object {
        actual fun load(data: ByteArray): ECPrivateKey {
            BouncycastleUtils.check()
            val c = KeyFactory.getInstance("EC", BouncycastleUtils.provider)
            val key = c.generatePrivate(java.security.spec.PKCS8EncodedKeySpec(data))
            return ECPrivateKey(key as BCECPrivateKey)
        }
    }
}
