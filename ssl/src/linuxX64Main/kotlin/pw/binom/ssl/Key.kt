package pw.binom.ssl

import kotlinx.cinterop.convert
import platform.openssl.*
import pw.binom.io.use

actual fun Key.Companion.generateRsa(size: Int): Key.Pair {
    val rsa = RSA_generate_key(size, RSA_F4.convert(), null, null)
    val publicKey = Bio.mem().use { b ->

        PEM_write_bio_RSAPublicKey(b.self, rsa)
//        i2d_RSAPublicKey_bio(b.self, rsa)
//        PEM_write_bio_RSAPublicKey(b.self, rsa)
        Key.Public(
            algorithm = KeyAlgorithm.RSA,
            data = b.toByteArray()
        )
    }

    val privateKey = Bio.mem().use { b ->
        PEM_write_bio_RSAPrivateKey(b.self, rsa, null, null, 0, null, null)
//        i2d_RSAPrivateKey_bio(b.self, rsa)
//        PEM_write_bio_RSAPrivateKey(b.self, rsa,null,null,null)
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
