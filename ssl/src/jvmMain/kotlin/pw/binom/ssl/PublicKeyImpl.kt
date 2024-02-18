package pw.binom.ssl

import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec

class PublicKeyImpl(
  override val algorithm: KeyAlgorithm,
  override val native: java.security.PublicKey,
) : PublicKey {
  override val data: ByteArray
    get() = native.encoded

  override fun close() {
    // NOP
  }
}

actual fun PublicKey.Companion.loadRSAFromContent(data: ByteArray): PublicKey {
  val kf = KeyFactory.getInstance("RSA")
  val vv = kf.generatePublic(PKCS8EncodedKeySpec(data))
  return PublicKeyImpl(algorithm = KeyAlgorithm.RSA, native = vv)
}

actual fun PublicKey.Companion.loadRSAFromPem(data: ByteArray): PublicKey {
  val o = ByteArrayInputStream(data)
  val r = org.bouncycastle.util.io.pem.PemReader(InputStreamReader(o))
  val pubKey = r.readPemObject() ?: throw IllegalArgumentException("Can't load public key from pem")
  val kf = KeyFactory.getInstance("RSA")
  val vv = kf.generatePublic(PKCS8EncodedKeySpec(pubKey.content))
  return PublicKeyImpl(algorithm = KeyAlgorithm.RSA, native = vv)
}
