package pw.binom.crypto

import pw.binom.ssl.ECKey
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class ECPublicKey : Key.Public, ECKey {
    val q: EcPoint

    companion object {
        fun load(data: ByteArray): ECPublicKey
    }

  override val format: String
  override val algorithm: KeyAlgorithm
  override val data: ByteArray
}
