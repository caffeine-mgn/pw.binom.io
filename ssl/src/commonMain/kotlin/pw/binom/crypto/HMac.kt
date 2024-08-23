package pw.binom.crypto

import pw.binom.security.MessageDigest

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class HMac : MessageDigest {
    constructor(algorithm: AlgorithmType, key: ByteArray)

    enum class AlgorithmType {
        SHA512, SHA384, SHA256, SHA1, MD5;

        companion object {
            fun find(name: String): AlgorithmType?

            /**
             * @throws NoSuchAlgorithmException throws when algorithm [name] not found
             */
            fun get(name: String): AlgorithmType
        }
    }

  override fun finish(): ByteArray
  override fun init()
  override fun update(byte: Byte)
}
