package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.base63.toJavaBigInteger
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateKey
import pw.binom.BouncycastleUtils
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm
import java.security.KeyFactory

actual class RSAPrivateKey(val native: BCRSAPrivateKey) : Key.Private {
    override val algorithm: KeyAlgorithm
        get() = KeyAlgorithm.RSA
    override val data: ByteArray
        get() = native.encoded
    actual val e: BigInteger
        get() = native.privateExponent.toBigInteger()
    actual val d: BigInteger
        get() = native.modulus.toBigInteger()
    actual val n: BigInteger
        get() = TODO("Not yet implemented")

    actual companion object {
        actual fun load(encodedKeySpec: KeySpec) =
            when (encodedKeySpec) {
                is PKCS8EncodedKeySpec -> {
                    BouncycastleUtils.check()
                    val c = KeyFactory.getInstance("RSA", "BC")
                    val key = c.generatePrivate(java.security.spec.PKCS8EncodedKeySpec(encodedKeySpec.data))
                    RSAPrivateKey(key as BCRSAPrivateKey)
                }
                is RSAPrivateKeySpec -> {
                    val c = KeyFactory.getInstance("RSA", "BC")
                    val key = c.generatePrivate(
                        java.security.spec.RSAPrivateKeySpec(
                            encodedKeySpec.modulus.toJavaBigInteger(),
                            encodedKeySpec.privateExponent.toJavaBigInteger()
                        )
                    )
                    RSAPrivateKey(key as BCRSAPrivateKey)
                }
            }
    }
}
