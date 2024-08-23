package pw.binom.ssl

import pw.binom.io.Closeable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object KeyGenerator {

  fun generate(algorithm: KeyAlgorithm, keySize: Int): KeyPair

  class KeyPair : Closeable {
    fun createPrivateKey(): PrivateKey
    override fun close()
  }
}
