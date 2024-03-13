@file:OptIn(ExperimentalForeignApi::class)

package pw.binom.ssl

import cnames.structs.stack_st_X509
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.openssl.*
import pw.binom.io.Closeable
import pw.binom.io.use

actual class X509Certificate internal constructor(val ptr: CPointer<X509>) : Closeable {
  override fun close() {
    X509_free(ptr)
  }

  actual companion object {
    actual fun load(data: ByteArray): X509Certificate {
      Bio.mem(data).use { bio ->
        bio.cursor = 0
        return X509Certificate(PEM_read_bio_X509(bio.self, null, null, null)!!)
      }
    }
  }

  actual fun save(): ByteArray =
    Bio.mem().use { bio ->
      PEM_write_bio_X509(bio.self, ptr)
      bio.toByteArray()
    }
}

inline val CPointer<stack_st_X509>.size: Int
  get() = internal_sk_X509_num(this).let { if (it < 0) 0 else it }
//    get() = sk_X509_num(this).let { if (it < 0) 0 else it }

inline operator fun CPointer<stack_st_X509>.get(index: Int): CPointer<X509> {
  if (index < 0 || index >= size) {
    throw IndexOutOfBoundsException()
  }
  return internal_sk_X509_value(this, index)!!
//    return sk_X509_value(this, index)!!
}

inline fun CPointer<stack_st_X509>.forEach(func: (CPointer<X509>) -> Unit) {
  for (i in 0 until size) {
    func(this[i])
  }
}
