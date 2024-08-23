package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class RSAPublicKey : Key.Public {
    val e: BigInteger
    val n: BigInteger
  override val format: String
  override val algorithm: KeyAlgorithm
  override val data: ByteArray
}
