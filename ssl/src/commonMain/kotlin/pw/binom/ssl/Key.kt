package pw.binom.ssl

sealed interface Key {
    val algorithm: KeyAlgorithm
    val data: ByteArray

    class Public(override val algorithm: KeyAlgorithm, override val data: ByteArray) : Key {
        companion object
    }

    class Private(override val algorithm: KeyAlgorithm, override val data: ByteArray) : Key {
        companion object
    }

    class Pair(val public: Public, val private: Private) {
        companion object
    }

    companion object
}

expect fun Key.Companion.generateRsa(size: Int): Key.Pair
expect fun Key.Companion.generateEcdsa(nid: Nid): Key.Pair
