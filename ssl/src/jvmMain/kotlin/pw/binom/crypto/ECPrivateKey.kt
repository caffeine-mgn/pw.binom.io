package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm

actual class ECPrivateKey(val native: BCECPrivateKey) : Key.Private {
    override val algorithm: KeyAlgorithm
        get() = KeyAlgorithm.ECDSA
    override val data: ByteArray
        get() = native.encoded

    actual val d: BigInteger by lazy {
        native.d.toBigInteger()
    }
}
