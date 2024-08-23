package pw.binom.ssl

import pw.binom.io.Closeable

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class X509Certificate : Closeable {
  companion object {
    fun load(data: ByteArray): X509Certificate
  }

  fun save(): ByteArray
  override fun close()
}
