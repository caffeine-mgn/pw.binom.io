package pw.binom.crypto

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.sha3_Init384
import pw.binom.security.MessageDigest

@OptIn(ExperimentalForeignApi::class)
actual class Keccak384MessageDigest : MessageDigest, AbstractKeccakMessageDigest() {
  override fun initContext(ctx: COpaquePointer) {
    sha3_Init384(ctx)
  }

  override val hashSize: Int
    get() = 384 / 8
}
