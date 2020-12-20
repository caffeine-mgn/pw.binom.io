package pw.binom.ssl

import pw.binom.io.Closeable

expect object KeyGenerator {

    fun generate(algorithm: KeyAlgorithm, keySize: Int): KeyPair

    class KeyPair : Closeable {
        fun createPrivateKey():PrivateKey
    }
}