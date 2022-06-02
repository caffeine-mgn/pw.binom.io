package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm

actual class RSAPublicKey(val native: BCRSAPublicKey) : Key.Public {
    override val algorithm: KeyAlgorithm
        get() = KeyAlgorithm.RSA
    override val data: ByteArray
        get() = TODO("Not yet implemented")
    override val format: String
        get() = "X.509"
    actual val e: BigInteger
        get() = native.publicExponent.toBigInteger()
    actual val n: BigInteger
        get() = native.modulus.toBigInteger()
}
