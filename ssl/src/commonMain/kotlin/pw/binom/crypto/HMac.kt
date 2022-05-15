package pw.binom.crypto

import pw.binom.security.MessageDigest

expect class HMac : MessageDigest {
    constructor(algorithm: Algorithm, key: ByteArray)

    enum class Algorithm {
        SHA512, SHA256, SHA1, MD5
    }
}
