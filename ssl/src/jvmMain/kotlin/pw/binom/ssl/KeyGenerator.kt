package pw.binom.ssl

import org.bouncycastle.jcajce.provider.asymmetric.dh.KeyPairGeneratorSpi
import pw.binom.io.Closeable

actual object KeyGenerator {

    actual fun generate(algorithm: KeyAlgorithm, keySize: Int): KeyPair {
        val keyPairGenerator = KeyPairGeneratorSpi.getInstance(algorithm.str)
        keyPairGenerator.initialize(keySize)
        return KeyPair(algorithm, keyPairGenerator.generateKeyPair())
    }

    actual class KeyPair(val algorithm: KeyAlgorithm, val native: java.security.KeyPair) : Closeable {
        override fun close() {
        }

        actual fun createPrivateKey(): PrivateKey = PrivateKeyImpl(algorithm = algorithm, native = native.private)
    }

}

val KeyAlgorithm.str: String
    get() = when (this) {
        KeyAlgorithm.RSA -> "RSA"
    }