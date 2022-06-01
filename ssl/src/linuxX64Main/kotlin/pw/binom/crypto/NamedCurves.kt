package pw.binom.crypto

import platform.openssl.*
import pw.binom.security.SecurityException
import pw.binom.ssl.Nid

actual object NamedCurves {

    actual fun getByName(name: String): X9ECParameters {
        val nid = Nid.fromString(name)
        val eckey = EC_KEY_new()
        if (eckey == null) {
            throw SecurityException("Failed to create new EC")
        }
        val ecgroup = EC_GROUP_new_by_curve_name(nid.toOpensslCurveName())
        return X9ECParameters(ecgroup!!, autoClean = true)
    }
}

fun Nid.Companion.fromString(name: String) = when (name) {
    "secp256r1" -> Nid.secp256r1
    "secp192k1" -> Nid.secp192k1
    "pkcs3" -> Nid.pkcs3
    "secp256k1" -> Nid.secp256k1
    else -> TODO("Nid \"$name\" not supported")
}

fun Nid.toOpensslCurveName() = when (this) {
    Nid.secp256r1 -> NID_X9_62_prime256v1
    Nid.secp192k1 -> NID_secp192k1
    Nid.pkcs3 -> NID_pkcs3
    Nid.secp256k1 -> NID_secp256k1
}
