package pw.binom.ssl

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.*
import pw.binom.io.ByteArrayOutput

@OptIn(ExperimentalForeignApi::class)
class PublicKeyImpl(override val algorithm: KeyAlgorithm, override val native: CPointer<EVP_PKEY>) : PublicKey {
  override val data: ByteArray
    get() {
      when (algorithm) {
        KeyAlgorithm.RSA -> {
          val rsa = EVP_PKEY_get1_RSA(native) ?: TODO("EVP_PKEY_get1_RSA returns null")
          val b = Bio.mem()
          i2d_RSAPublicKey_bio(b.self, rsa)
          val o = ByteArrayOutput()
          b.copyTo(o)
          b.close()
          o.data.flip()
          val array = o.data.toByteArray()
          o.close()
          return array
        }

        else -> TODO()
      }
    }

  override fun close() {
    TODO("Not yet implemented")
  }
}

@OptIn(ExperimentalForeignApi::class)
actual fun PublicKey.Companion.loadRSA(data: ByteArray): PublicKey {
  val b = Bio.mem(data)
  val rsa = d2i_RSAPublicKey_bio(b.self, null)
  val k = EVP_PKEY_new()!!
  EVP_PKEY_set1_RSA(k, rsa)
  return PublicKeyImpl(algorithm = KeyAlgorithm.RSA, native = k)
}
