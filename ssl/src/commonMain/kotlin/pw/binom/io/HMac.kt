package pw.binom.io

expect class HMac:MessageDigest {
    constructor(algorithm: Algorithm, key:ByteArray)

    enum class Algorithm {
        SHA512, SHA1
    }
}