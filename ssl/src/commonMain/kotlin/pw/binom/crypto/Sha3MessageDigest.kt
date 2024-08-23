package pw.binom.crypto

import pw.binom.security.MessageDigest

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class Sha3MessageDigest(size: Size) : MessageDigest {

    enum class Size {
        S224,
        S254,
        S256,
        S384,
        S512,
    }

  override fun init()
  override fun finish(): ByteArray
  override fun update(byte: Byte)
}
