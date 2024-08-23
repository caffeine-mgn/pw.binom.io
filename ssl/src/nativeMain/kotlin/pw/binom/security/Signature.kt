package pw.binom.security

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.*
import pw.binom.crypto.AlgorithmInstance
import pw.binom.io.ByteBuffer
import pw.binom.ssl.Key

const val RSA = "RSA"
const val ECDSA = "ECDSA"
const val SHA1 = "SHA1"
const val SHA3_224 = "SHA3-224"
const val SHA3_256 = "SHA3-256"
const val SHA3_384 = "SHA3-384"
const val SHA3_512 = "SHA3-512"
const val SHA256 = "SHA256"
const val SHA224 = "SHA224"
const val SHA384 = "SHA384"
const val SHA512 = "SHA512"
const val MD4 = "MD4"
const val MD5 = "MD5"
const val SM3 = "SM3"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual interface Signature {
  actual companion object {
    actual fun getInstance(algorithm: String): Signature = when (algorithm) {
      "${SHA1}with$ECDSA" -> SignatureEcdsa(AlgorithmInstance.sha1())
      "${SHA256}with$ECDSA" -> SignatureEcdsa(AlgorithmInstance.sha256())
      "${SHA224}with$ECDSA" -> SignatureEcdsa(AlgorithmInstance.sha224())
      "${SHA384}with$ECDSA" -> SignatureEcdsa(AlgorithmInstance.sha384())
      "${SHA512}with$ECDSA" -> SignatureEcdsa(AlgorithmInstance.sha512())
      "${SM3}with$ECDSA" -> SignatureEcdsa(AlgorithmInstance.sm3())
      "${SHA3_256}with$ECDSA" -> SignatureEcdsa(AlgorithmInstance.sha3_256())
      "${SHA3_224}with$ECDSA" -> SignatureEcdsa(AlgorithmInstance.sha3_224())
      "${SHA3_384}with$ECDSA" -> SignatureEcdsa(AlgorithmInstance.sha3_384())
      "${SHA3_512}with$ECDSA" -> SignatureEcdsa(AlgorithmInstance.sha3_512())

      "${SHA1}with$RSA" -> SignatureRsa(AlgorithmInstance.sha1())
      "${SHA256}with$RSA" -> SignatureRsa(AlgorithmInstance.sha256())
      "${SHA384}with$RSA" -> SignatureRsa(AlgorithmInstance.sha384())
      "${SHA512}with$RSA" -> SignatureRsa(AlgorithmInstance.sha512())

      else -> throw IllegalArgumentException("Algorithm \"$algorithm\" not supported")
    }
  }

  actual fun init(key: Key.Private)
  actual fun update(data: ByteArray)
  actual fun update(data: ByteBuffer)
  actual fun sign(): ByteArray
  actual fun init(key: Key.Public)
  actual fun verify(signature: ByteArray): Boolean
}

@OptIn(ExperimentalForeignApi::class)
fun CPointer<EVP_PKEY>.setKey(key: CPointer<RSA>) = EVP_PKEY_set1_RSA(this, key) == 1
