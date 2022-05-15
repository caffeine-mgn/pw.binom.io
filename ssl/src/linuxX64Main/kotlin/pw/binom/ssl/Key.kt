package pw.binom.ssl

import kotlinx.cinterop.convert
import platform.openssl.*
import pw.binom.base64.Base64
import pw.binom.getSslError
import pw.binom.io.use
import pw.binom.security.InvalidAlgorithmParameterException
import pw.binom.security.SecurityException

actual fun Key.Companion.generateRsa(size: Int): Key.Pair {
    val rsa = RSA_generate_key(size, RSA_F4.convert(), null, null)
    val publicKey = Bio.mem().use { b ->
        PEM_write_bio_RSA_PUBKEY(b.self, rsa)
        val data = b.toByteArray()
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
        val str = data.decodeToString().replace("\n", "").removePrefix("-----BEGIN RSA PRIVATE KEY-----")
            .removeSuffix("-----END RSA PRIVATE KEY-----")
        Key.Private(
            algorithm = KeyAlgorithm.RSA, data = Base64.decode(str)
        )
    }
    RSA_free(rsa)
    return Key.Pair(
        public = publicKey, private = privateKey
    )
}

actual fun Key.Companion.generateEcdsa(nid: Nid): Key.Pair {
    val nidValue = when (nid) {
        Nid.secp256r1 -> NID_X9_62_prime256v1
        Nid.secp192k1 -> NID_secp192k1
        Nid.pkcs3 -> NID_pkcs3
        Nid.secp256k1 -> NID_secp256k1
    }
    val eckey = EC_KEY_new()
    if (eckey == null) {
        throw SecurityException("Failed to create new EC")
    }
    val ecgroup = EC_GROUP_new_by_curve_name(nidValue)
    if (ecgroup == null) {
        EC_KEY_free(eckey)
        throw InvalidAlgorithmParameterException("Failed to create new EC Group with nid $nid: ${getSslError()}")
    }

    if (EC_KEY_set_group(eckey, ecgroup) != 1) {
        EC_KEY_free(eckey)
        EC_GROUP_free(ecgroup)
        throw SecurityException("Failed to set group for EC Key")
    }
    if (EC_KEY_generate_key(eckey) != 1) {
        EC_KEY_free(eckey)
        throw SecurityException("Failed to generate EC Key")
    }
    val publicKey = Bio.mem().use { b ->
        PEM_write_bio_EC_PUBKEY(b.self, eckey)
        b.toByteArray()
    }.decodeToString().replace("\n", "").removePrefix("-----BEGIN PUBLIC KEY-----")
        .removeSuffix("-----END PUBLIC KEY-----").let {
            Key.Public(
                algorithm = KeyAlgorithm.ECDSA,
                data = Base64.decode(it),
            )
        }

    val privateKey = Bio.mem().use { b ->
        PEM_write_bio_ECPrivateKey(b.self, eckey, null, null, 0, null, null)
        b.toByteArray()
    }.decodeToString()
        .replace("\n", "")
        .removePrefix("-----BEGIN EC PRIVATE KEY-----")
        .removeSuffix("-----END EC PRIVATE KEY-----").let {
            Key.Private(
                algorithm = KeyAlgorithm.ECDSA, data = Base64.decode(it)
            )
        }
    EC_KEY_free(eckey)
    return Key.Pair(
        public = publicKey, private = privateKey
    )
}
