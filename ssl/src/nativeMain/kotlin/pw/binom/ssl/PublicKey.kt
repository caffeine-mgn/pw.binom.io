package pw.binom.ssl

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.EVP_PKEY
import pw.binom.io.Closeable

@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual interface PublicKey : Closeable {
  actual companion object

  val native: CPointer<EVP_PKEY>
  actual val algorithm: KeyAlgorithm
  actual val data: ByteArray
}
