package pw.binom.ssl

import kotlinx.cinterop.convert
import platform.openssl.PEM_write_bio_RSAPrivateKey
import platform.openssl.PEM_write_bio_RSA_PUBKEY
import platform.openssl.RSA_F4
import platform.openssl.RSA_generate_key
import pw.binom.base64.Base64
import pw.binom.io.use

actual fun Key.Companion.generateRsa(size: Int): Key.Pair {
    val rsa = RSA_generate_key(size, RSA_F4.convert(), null, null)
    val publicKey = Bio.mem().use { b ->

//        PEM_write_bio_RSAPublicKey(b.self, rsa)
        PEM_write_bio_RSA_PUBKEY(b.self, rsa)
//        i2d_RSAPublicKey_bio(b.self, rsa)
//        PEM_write_bio_RSAPublicKey(b.self, rsa)
        val data = b.toByteArray()

        println("Public Key Data: ${data.decodeToString()}")

        println("Generated public key: \"${data.decodeToString()}\"")
        val str = data.decodeToString()
            .replace("\n", "")
            .removePrefix("-----BEGIN PUBLIC KEY-----")
            .removeSuffix("-----END PUBLIC KEY-----")
        Key.Public(
            algorithm = KeyAlgorithm.RSA,
            data = Base64.decode(str),
        )
    }

    val privateKey = Bio.mem().use { b ->
        PEM_write_bio_RSAPrivateKey(b.self, rsa, null, null, 0, null, null)
        val data = b.toByteArray()
        val str = data.decodeToString()
            .replace("\n", "")
            .removePrefix("-----BEGIN RSA PRIVATE KEY-----")
            .removeSuffix("-----END RSA PRIVATE KEY-----")
        Key.Private(
            algorithm = KeyAlgorithm.RSA,
            data = Base64.decode(str)
        )
    }
    return Key.Pair(
        public = publicKey,
        private = privateKey
    )
}
