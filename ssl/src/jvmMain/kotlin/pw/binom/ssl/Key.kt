package pw.binom.ssl

import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec

actual fun Key.Companion.generateRsa(size: Int): Key.Pair {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(size)
    val pair = keyPairGenerator.generateKeyPair()
    return Key.Pair(
        public = Key.Public(algorithm = KeyAlgorithm.RSA, data = pair.public.encoded),
        private = Key.Private(algorithm = KeyAlgorithm.RSA, data = pair.private.encoded),
    )
}

actual fun Key.Companion.generateEcdsa(nid: Nid): Key.Pair {
    val nidValue = when (nid) {
        Nid.secp256r1 -> "secp256r1"
        Nid.secp192k1 -> "secp192k1"
        Nid.pkcs3 -> "pkcs3"
        Nid.secp256k1 -> "secp256k1"
    }
    val keyPairGenerator = KeyPairGenerator.getInstance("EC")
    keyPairGenerator.initialize(ECGenParameterSpec(nidValue), SecureRandom())
    val pair = keyPairGenerator.generateKeyPair()
    return Key.Pair(
        public = Key.Public(algorithm = KeyAlgorithm.ECDSA, data = pair.public.encoded),
        private = Key.Private(algorithm = KeyAlgorithm.ECDSA, data = pair.private.encoded),
    )
}
