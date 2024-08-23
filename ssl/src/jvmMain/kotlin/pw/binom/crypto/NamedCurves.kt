package pw.binom.crypto

import org.bouncycastle.crypto.ec.CustomNamedCurves
import pw.binom.ssl.Nid

actual object NamedCurves {
  actual fun getByName(nid: Nid): X9ECParameters =
    X9ECParameters(
      CustomNamedCurves.getByName(
        when (nid) {
          Nid.secp256k1 -> "secp256k1"
          Nid.secp256r1 -> "secp256r1"
          Nid.secp192k1 -> "secp192k1"
          Nid.pkcs3 -> "pkcs3"
          Nid.secp384r1 -> "secp384r1"
          Nid.secp521r1 -> "secp521r1"
        }
      )
    )
}
