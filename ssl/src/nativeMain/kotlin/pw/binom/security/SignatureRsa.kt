package pw.binom.security

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.EVP_PKEY
import platform.openssl.EVP_PKEY_free
import platform.openssl.EVP_PKEY_new
import pw.binom.crypto.AlgorithmInstance
import pw.binom.crypto.RSAPrivateKey
import pw.binom.ssl.Key
import pw.binom.ssl.KeyAlgorithm
import pw.binom.ssl.createRsaFromPrivateKey
import pw.binom.ssl.createRsaFromPublicKey

@OptIn(ExperimentalForeignApi::class)
class SignatureRsa(messageDigest: AlgorithmInstance) : AbstractSignature(messageDigest) {
  override fun isAlgorithmSupport(algorithm: KeyAlgorithm) = algorithm == KeyAlgorithm.RSA

  override fun createPkey(key: Key.Private): CPointer<EVP_PKEY> {
    key as RSAPrivateKey
    val ecKey = createRsaFromPrivateKey(key.data)
    val pkey = EVP_PKEY_new()!!
    if (!pkey.setKey(ecKey)) {
      EVP_PKEY_free(pkey)
      throw SignatureException("EVP_PKEY_set1_EC_KEY failed")
    }
    return pkey
  }

  override fun createPkey(key: Key.Public): CPointer<EVP_PKEY> {
    val ecKey = createRsaFromPublicKey(key.data)
    val pkey = EVP_PKEY_new()!!
    if (!pkey.setKey(ecKey)) {
      EVP_PKEY_free(pkey)
      throw SignatureException("EVP_PKEY_set1_EC_KEY failed")
    }
    return pkey
  }
}
