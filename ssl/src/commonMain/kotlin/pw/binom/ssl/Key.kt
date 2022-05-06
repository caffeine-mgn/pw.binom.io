package pw.binom.ssl

sealed interface Key {
    val algorithm: KeyAlgorithm
    val data: ByteArray

    class Public(override val algorithm: KeyAlgorithm, override val data: ByteArray) : Key
    class Private(override val algorithm: KeyAlgorithm, override val data: ByteArray) : Key
    class Pair(val public: Public, val private: Private)
    companion object;
}

expect fun Key.Companion.generateRsa(size: Int): Key.Pair
