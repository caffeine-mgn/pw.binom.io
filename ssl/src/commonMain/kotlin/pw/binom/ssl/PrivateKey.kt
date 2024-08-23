package pw.binom.ssl

import pw.binom.io.Closeable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect interface PrivateKey : Closeable {
  companion object

  val algorithm: KeyAlgorithm
  val data: ByteArray
}

expect fun PrivateKey.Companion.loadRSAFromPem(data: ByteArray): PrivateKey
