package pw.binom.ssl

import java.security.KeyPairGenerator

actual fun Key.Companion.generateRsa(size: Int): Key.Pair {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(size)
    val pair = keyPairGenerator.generateKeyPair()

    return Key.Pair(
        public = Key.Public(algorithm = KeyAlgorithm.RSA, data = pair.public.encoded),
        private = Key.Private(algorithm = KeyAlgorithm.RSA, data = pair.private.encoded),
    )
}
