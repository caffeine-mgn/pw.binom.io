package pw.binom.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.*
import pw.binom.ssl.Nid
import pw.binom.throwError

@OptIn(ExperimentalForeignApi::class)
actual object NamedCurves {

  actual fun getByName(nid: Nid): X9ECParameters {
    val eckey = EC_KEY_new() ?: throwError("Failed to create new EC")
    val ecgroup =
      EC_GROUP_new_by_curve_name(nid.toOpensslCurveName()) ?: throwError("EC_GROUP_new_by_curve_name fails")
    return X9ECParameters(ECCurve(ecgroup))
  }
}

fun Nid.Companion.fromString(name: String) = when (name) {
  "secp256r1" -> Nid.secp256r1
  "secp192k1" -> Nid.secp192k1
  "pkcs3" -> Nid.pkcs3
  "secp256k1" -> Nid.secp256k1
  else -> TODO("Nid \"$name\" not supported")
}

@OptIn(ExperimentalForeignApi::class)
fun Nid.toOpensslCurveName() = when (this) {
  Nid.secp256r1 -> NID_X9_62_prime256v1
  Nid.secp192k1 -> NID_secp192k1
  Nid.pkcs3 -> NID_pkcs3
  Nid.secp256k1 -> NID_secp256k1
}
