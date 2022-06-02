package pw.binom.ssl

import pw.binom.crypto.ECPrivateKey
import pw.binom.crypto.ECPublicKey
import pw.binom.crypto.RSAPrivateKey
import pw.binom.crypto.RSAPublicKey

sealed interface Key {
    val algorithm: KeyAlgorithm
    val data: ByteArray
    val format: String

    interface Public : Key {
        companion object
    }

    interface Private : Key {
        companion object
    }

    class Pair<PUBLIC : Key.Public, PRIVATE : Key.Private>(val public: PUBLIC, val private: PRIVATE) {
        companion object
    }

    companion object
}

expect fun Key.Companion.generateRsa(size: Int): Key.Pair<RSAPublicKey, RSAPrivateKey>
expect fun Key.Companion.generateEcdsa(nid: Nid): Key.Pair<ECPublicKey, ECPrivateKey>
