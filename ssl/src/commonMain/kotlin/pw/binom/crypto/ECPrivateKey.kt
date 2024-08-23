package pw.binom.crypto

import com.ionspin.kotlin.bignum.integer.BigInteger
import pw.binom.ssl.ECKey
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class ECPrivateKey : Key.Private, ECKey {
    val d: BigInteger

    companion object {
        fun load(data: ByteArray): ECPrivateKey
    }

  override val algorithm: KeyAlgorithm
  override val data: ByteArray
  override val format: String
}
