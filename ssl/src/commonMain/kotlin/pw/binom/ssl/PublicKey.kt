package pw.binom.ssl

import pw.binom.io.Closeable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect interface PublicKey : Closeable {
  companion object

  val algorithm: KeyAlgorithm
  val data: ByteArray
}

expect fun PublicKey.Companion.loadRSAFromContent(data: ByteArray): PublicKey

expect fun PublicKey.Companion.loadRSAFromPem(data: ByteArray): PublicKey
