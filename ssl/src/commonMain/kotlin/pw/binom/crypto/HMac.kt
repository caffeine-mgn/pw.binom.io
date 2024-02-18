package pw.binom.crypto

import pw.binom.security.MessageDigest

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
}
