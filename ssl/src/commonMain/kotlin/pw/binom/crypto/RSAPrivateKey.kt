package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class RSAPrivateKey : Key.Private {
    companion object {
        fun load(encodedKeySpec: KeySpec): RSAPrivateKey
    }

    val n: BigInteger
    val e: BigInteger
    val d: BigInteger
  override val algorithm: KeyAlgorithm
  override val data: ByteArray
  override val format: String
}
