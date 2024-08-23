package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm

actual class RSAPublicKey(val native: BCRSAPublicKey) : Key.Public {
    actual override val algorithm: KeyAlgorithm
        get() = KeyAlgorithm.RSA
    actual override val data: ByteArray
        get() = native.encoded
    actual override val format: String
        get() = "X.509"
    actual val e: BigInteger
        get() = native.publicExponent.toBigInteger()
    actual val n: BigInteger
        get() = native.modulus.toBigInteger()
}
