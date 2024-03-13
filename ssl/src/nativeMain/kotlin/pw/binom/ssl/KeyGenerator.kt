package pw.binom.ssl

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import platform.openssl.*
import pw.binom.io.Closeable

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual object KeyGenerator {

  actual fun generate(algorithm: KeyAlgorithm, keySize: Int): KeyPair {
    val pk = EVP_PKEY_new()!!
    when (algorithm) {
      KeyAlgorithm.RSA -> {
        RSA_new()
        val rsa = RSA_generate_key(keySize, RSA_F4.convert(), null, null)
        if (EVP_PKEY_assign(pk, EVP_PKEY_RSA, rsa) <= 0) {
          TODO("EVP_PKEY_assign error")
        }
      }

      KeyAlgorithm.ECDSA -> TODO()
    }
    return KeyPair(pk)
  }

  actual class KeyPair(val native: CPointer<EVP_PKEY>) : Closeable {
    override fun close() {
      EVP_PKEY_free(native)
    }

    actual fun createPrivateKey(): PrivateKey = PrivateKeyImpl(algorithm = KeyAlgorithm.RSA, native = native.copy())
  }
}

@OptIn(ExperimentalForeignApi::class)
fun CPointer<EVP_PKEY>.copy(): CPointer<EVP_PKEY> {
  EVP_PKEY_up_ref(this)
  return this
}
