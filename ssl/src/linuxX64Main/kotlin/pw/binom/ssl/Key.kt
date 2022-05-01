package pw.binom.ssl

import kotlinx.cinterop.convert
import platform.openssl.*
import pw.binom.io.use

actual fun Key.Companion.generateRsa(size: Int): Key.Pair {
    val rsa = RSA_generate_key(size, RSA_F4.convert(), null, null)
    val publicKey = Bio.mem().use { b ->
        i2d_RSAPublicKey_bio(b.self, rsa)
        Key.Public(
            algorithm = KeyAlgorithm.RSA,
            data = b.toByteArray()
        )
    }

    val privateKey = Bio.mem().use { b ->
        i2d_RSAPrivateKey_bio(b.self, rsa)
        Key.Private(
            algorithm = KeyAlgorithm.RSA,
            data = b.toByteArray()
        )
    }
    return Key.Pair(
        public = publicKey,
        private = privateKey
    )
}
