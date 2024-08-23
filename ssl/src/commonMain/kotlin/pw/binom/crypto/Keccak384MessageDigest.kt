package pw.binom.crypto

import pw.binom.security.MessageDigest

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class Keccak384MessageDigest : MessageDigest {
    constructor()

  override fun finish(): ByteArray
  override fun init()
  override fun update(byte: Byte)
}
