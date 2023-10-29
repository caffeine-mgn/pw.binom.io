package pw.binom.security

import kotlinx.cinterop.CPointer
import platform.openssl.*
import pw.binom.crypto.AlgorithmInstance
import pw.binom.crypto.ECPrivateKey
import pw.binom.getSslError
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm
import pw.binom.ssl.createEcdsaFromPublicKey
import pw.binom.throwError

class SignatureEcdsa(messageDigest: AlgorithmInstance) : AbstractSignature(messageDigest) {
  override fun isAlgorithmSupport(algorithm: KeyAlgorithm) = algorithm == KeyAlgorithm.ECDSA

  override fun createPkey(key: Key.Private): CPointer<EVP_PKEY> {
    key as ECPrivateKey
    val pkey = EVP_PKEY_new() ?: throwError("EVP_PKEY_new vails")
    val dup = EC_KEY_dup(key.native) ?: throwError("EC_KEY_dup fails")
    if (!pkey.setKey(dup)) {
      EC_KEY_free(dup)
      EVP_PKEY_free(pkey)
      throw SignatureException("EVP_PKEY_set1_EC_KEY failed")
    }
    return pkey
  }

  override fun createPkey(key: Key.Public): CPointer<EVP_PKEY> {
    println("-->1# getSslError()->${getSslError()}")
    val ecKey = createEcdsaFromPublicKey(key.data)
    println("-->2# getSslError()->${getSslError()}")
    val pkey = EVP_PKEY_new()!!
    println("-->3# getSslError()->${getSslError()}")
    if (!pkey.setKey(ecKey)) {
      EVP_PKEY_free(pkey)
      throw SignatureException("EVP_PKEY_set1_EC_KEY failed")
    }
    return pkey
  }
}

private fun CPointer<EVP_PKEY>.setKey(key: CPointer<EC_KEY>) = EVP_PKEY_set1_EC_KEY(this, key) == 1
