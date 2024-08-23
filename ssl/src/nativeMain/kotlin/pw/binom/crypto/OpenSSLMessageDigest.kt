@file:OptIn(ExperimentalNativeApi::class)

package pw.binom.crypto

import kotlinx.cinterop.*
import platform.openssl.*
import pw.binom.checkTrue
import pw.binom.io.ByteBuffer
import pw.binom.security.MessageDigest
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
abstract class OpenSSLMessageDigest : MessageDigest {

  private class Resource(
    var ptr: CPointer<EVP_MD_CTX>? = null,
  ) {
    fun init(): Boolean {
      return if (ptr == null) {
        ptr = EVP_MD_CTX_new()
        true
      } else {
        false
      }
    }

    fun free() {
      if (ptr != null) {
        EVP_MD_CTX_free(ptr)
        ptr = null
      }
    }
  }

  private val resource = Resource()

  @OptIn(ExperimentalStdlibApi::class)
  private val cleaner = createCleaner(resource) {
    it.free()
  }

  protected abstract fun createEvp(): CPointer<EVP_MD>
  protected abstract val finalByteArraySize: Int
  override fun init() {
    if (resource.ptr != null) {
      EVP_MD_CTX_free(resource.ptr)
      resource.ptr = null
    }
    checkInit()
  }

  private fun checkInit() {
    if (resource.init()) {
      EVP_DigestInit_ex(resource.ptr, createEvp(), null).checkTrue("EVP_DigestInit_ex") {
        resource.free()
      }
    }
  }

  override fun update(byte: Byte) {
    checkInit()
    memScoped {
      val ar = allocArray<UByteVar>(1)
      ar[0] = byte.toUByte()
      EVP_DigestUpdate(resource.ptr, ar, 1.convert())
    }
  }

  override fun update(input: ByteArray, offset: Int, len: Int) {
    checkInit()
    if (input.isNotEmpty()) {
      input.usePinned { p ->
        EVP_DigestUpdate(resource.ptr, p.addressOf(offset), len.convert())
      }
    }
  }

  override fun update(byte: ByteArray) {
    super.update(byte)
  }

  override fun update(buffer: ByteBuffer) {
    checkInit()
    if (buffer.remaining > 0) {
      buffer.ref(0) { bufferPtr, remaining ->
        EVP_DigestUpdate(resource.ptr, bufferPtr, remaining.convert())
      }
      buffer.position = buffer.limit
    }
  }

  override fun finish(): ByteArray {
    checkInit()
    val out = ByteArray(finalByteArraySize)
    memScoped {
      val size = alloc<UIntVar>()
      EVP_DigestFinal(resource.ptr, out.refTo(0).getPointer(this).reinterpret(), size.ptr)
        .checkTrue("EVP_DigestFinal fails")
      if (size.value != out.size.convert<UInt>()) {
        TODO()
      }
    }
    resource.free()
    return out
  }
}
