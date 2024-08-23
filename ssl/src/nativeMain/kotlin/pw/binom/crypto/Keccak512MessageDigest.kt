package pw.binom.crypto

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.sha3_Init512
import pw.binom.security.MessageDigest

@OptIn(ExperimentalForeignApi::class)
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class Keccak512MessageDigest : MessageDigest, AbstractKeccakMessageDigest() {
  override fun initContext(ctx: COpaquePointer) {
    sha3_Init512(ctx)
  }

  override val hashSize: Int
    get() = 512 / 8
}
