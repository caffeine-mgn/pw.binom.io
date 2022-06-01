package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm

actual class ECPrivateKey(actual val d: BigInteger) : Key.Private {
    override val algorithm: KeyAlgorithm
        get() = KeyAlgorithm.ECDSA
    override val data: ByteArray
        get() = TODO("Not yet implemented")
}
