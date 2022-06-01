package pw.binom.ssl

import kotlinx.cinterop.convert
import platform.openssl.*
import pw.binom.BigNum
import pw.binom.base64.Base64
import pw.binom.checkTrue
import pw.binom.crypto.*
import pw.binom.getSslError
import pw.binom.io.use
import pw.binom.security.InvalidAlgorithmParameterException
import pw.binom.security.SecurityException
import pw.binom.throwError

actual fun Key.Companion.generateRsa(size: Int): Key.Pair<RSAPublicKey, RSAPrivateKey> {
    val rsa = RSA_generate_key(size, RSA_F4.convert(), null, null) ?: throwError("RSA_generate_key fail")
    val e = BigNum(RSA_get0_e(rsa)!!).toBigInt() // public
    val n = BigNum(RSA_get0_n(rsa)!!).toBigInt() // public/private
    val d = BigNum(RSA_get0_d(rsa)!!).toBigInt() // private
//    val p = BigNum(RSA_get0_p(rsa)!!).toBigInt()
//    val q = BigNum(RSA_get0_q(rsa)!!).toBigInt()

    val privateKeyBytes = Bio.mem().use { b ->
        PEM_write_bio_RSAPrivateKey(
            b.self,
            rsa,
            null,
            null,
            0,
            null,
            null
        ).checkTrue("PEM_write_bio_RSAPrivateKey fail")
        b.toByteArray().decodeToString().also {
            println("->$it")
        }.replace("\n", "").removePrefix("-----BEGIN RSA PRIVATE KEY-----")
            .removeSuffix("-----END RSA PRIVATE KEY-----").also {
                println("->$it")
            }.let { Base64.decode(it) }
    }

    val privateKeyReaded = createRsaFromPrivateKey(privateKeyBytes)
    println("e=${RSA_get0_e(privateKeyReaded)}")
    println("n=${RSA_get0_n(privateKeyReaded)}")
    println("d=${RSA_get0_d(privateKeyReaded)}")
    println("readed success")

    RSA_free(rsa)
    return Key.Pair(
        public = RSAPublicKey(
            e = e,
            n = n,
        ),
        private = RSAPrivateKey(
            e = e,
            d = d,
            n = n,
        ),
    )

//    val publicKey = Bio.mem().use { b ->
//        PEM_write_bio_RSA_PUBKEY(b.self, rsa)
//        val data = b.toByteArray()
//        val str = data.decodeToString()
//            .replace("\n", "")
//            .removePrefix("-----BEGIN PUBLIC KEY-----")
//            .removeSuffix("-----END PUBLIC KEY-----")
//        Key.Public(
//            algorithm = KeyAlgorithm.RSA,
//            data = Base64.decode(str),
//        )
//    }

//    val privateKey = Bio.mem().use { b ->
//        PEM_write_bio_RSAPrivateKey(b.self, rsa, null, null, 0, null, null)
//        val data = b.toByteArray()
//        val str = data.decodeToString().replace("\n", "").removePrefix("-----BEGIN RSA PRIVATE KEY-----")
//            .removeSuffix("-----END RSA PRIVATE KEY-----")
//        Key.Private(
//            algorithm = KeyAlgorithm.RSA, data = Base64.decode(str)
//        )
//    }
//    RSA_free(rsa)
//    return Key.Pair(
//        public = publicKey, private = privateKey
//    )
}

actual fun Key.Companion.generateEcdsa(nid: Nid): Key.Pair<ECPublicKey, ECPrivateKey> {
    val eckey = EC_KEY_new()
    if (eckey == null) {
        throw SecurityException("Failed to create new EC")
    }
    val ecgroup = EC_GROUP_new_by_curve_name(nid.toOpensslCurveName())
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
    val privateKeyBigInteger = BigNum(EC_KEY_get0_private_key(eckey) ?: TODO("Can't get private key")).toBigInt()
    val publicKey2 = EC_KEY_get0_public_key(eckey)
    val newGroup = EC_GROUP_dup(ecgroup)!!
    val curve = ECCurve(newGroup)

    EC_KEY_free(eckey)
    return Key.Pair(
        public = ECPublicKey(
            curve = curve,
            q = EcPoint(
                curve = curve,
                ptr = EC_POINT_dup(publicKey2, curve.native) ?: throwError("EC_POINT_dup fails")
            )
        ),
        private = ECPrivateKey(d = privateKeyBigInteger)
    )

//    println("publicBiginteger->$publicBiginteger")
//    val publicKey = Bio.mem().use { b ->
//        PEM_write_bio_EC_PUBKEY(b.self, eckey)
//        b.toByteArray()
//    }.decodeToString().replace("\n", "").removePrefix("-----BEGIN PUBLIC KEY-----")
//        .removeSuffix("-----END PUBLIC KEY-----").let {
//            Key.Public(
//                algorithm = KeyAlgorithm.ECDSA,
//                data = Base64.decode(it),
//            )
//        }
//
//    val privateKey = Bio.mem().use { b ->
//        PEM_write_bio_ECPrivateKey(b.self, eckey, null, null, 0, null, null)
//        b.toByteArray()
//    }.decodeToString()
//        .replace("\n", "")
//        .removePrefix("-----BEGIN EC PRIVATE KEY-----")
//        .removeSuffix("-----END EC PRIVATE KEY-----").let {
//            Key.Private(
//                algorithm = KeyAlgorithm.ECDSA, data = Base64.decode(it)
//            )
//        }
//    EC_KEY_free(eckey)
//    return Key.Pair(
//        public = publicKey, private = privateKey
//    )
}
