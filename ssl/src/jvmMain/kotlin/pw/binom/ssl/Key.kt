package pw.binom.ssl

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateKey
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey
import pw.binom.BouncycastleUtils
import pw.binom.crypto.ECPrivateKey
import pw.binom.crypto.ECPublicKey
import pw.binom.crypto.RSAPrivateKey
import pw.binom.crypto.RSAPublicKey
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec

actual fun Key.Companion.generateRsa(size: Int): Key.Pair<RSAPublicKey, RSAPrivateKey> {
  BouncycastleUtils.check()
  val keyPairGenerator = KeyPairGenerator.getInstance("RSA", BouncycastleUtils.provider)
  keyPairGenerator.initialize(size)
  val pair = keyPairGenerator.generateKeyPair()
  val public = pair.public as BCRSAPublicKey
  val private = pair.private as BCRSAPrivateKey
  return Key.Pair<RSAPublicKey, RSAPrivateKey>(
    public = RSAPublicKey(native = public),
    private = RSAPrivateKey(native = private),
  )
}

actual fun Key.Companion.generateEcdsa(nid: Nid): Key.Pair<ECPublicKey, ECPrivateKey> {
  val nidValue = when (nid) {
    Nid.secp256r1 -> "secp256r1"
    Nid.secp192k1 -> "secp192k1"
    Nid.secp384r1 -> "secp384r1"
    Nid.secp521r1 -> "secp521r1"
    Nid.pkcs3 -> "pkcs3"
    Nid.secp256k1 -> "secp256k1"
  }
  BouncycastleUtils.check()
  val keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", BouncycastleUtils.provider)
  keyPairGenerator.initialize(ECGenParameterSpec(nidValue), SecureRandom())
  val pair = keyPairGenerator.generateKeyPair()
  val public = pair.public as BCECPublicKey
  val private = pair.private as BCECPrivateKey
  return Key.Pair(
    public = ECPublicKey(native = public),
    private = ECPrivateKey(native = private),
  )
}
