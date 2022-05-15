package pw.binom.crypto

import pw.binom.security.MessageDigest

expect class Sha3MessageDigest(size: Size) : MessageDigest {

    enum class Size {
        S224,
        S254,
        S256,
        S384,
        S512,
    }
}
