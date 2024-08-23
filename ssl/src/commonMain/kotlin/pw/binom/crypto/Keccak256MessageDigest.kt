package pw.binom.crypto

import pw.binom.security.MessageDigest

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class Keccak256MessageDigest : MessageDigest {
    constructor()

  override fun init()
  override fun finish(): ByteArray
  override fun update(byte: Byte)
}
