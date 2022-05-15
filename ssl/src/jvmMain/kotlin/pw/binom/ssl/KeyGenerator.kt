package pw.binom.ssl

import pw.binom.io.Closeable
import java.security.KeyPairGenerator

actual object KeyGenerator {

    actual fun generate(algorithm: KeyAlgorithm, keySize: Int): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(algorithm.str)
        keyPairGenerator.initialize(keySize)
        return KeyPair(algorithm, keyPairGenerator.generateKeyPair())
    }

    actual class KeyPair(val algorithm: KeyAlgorithm, val native: java.security.KeyPair) : Closeable {
        override fun close() {
        }

        actual fun createPrivateKey(): PrivateKey = PrivateKeyImpl(algorithm = algorithm, native = native.private)
    }
}

private val KeyAlgorithm.str: String
    get() = when (this) {
        KeyAlgorithm.RSA -> "RSA"
        KeyAlgorithm.ECDSA -> "EC"
    }
