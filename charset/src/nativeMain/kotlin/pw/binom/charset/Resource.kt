package pw.binom.charset

import kotlinx.cinterop.*
import platform.binomiconv.*
import platform.posix.EBADF
import platform.posix.errno
import platform.posix.set_posix_errno
import platform.posix.size_tVar

@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
internal class Resource(fromCharset: String, toCharset: String) {

  //        val key = "$fromCharset..$toCharset"
  @OptIn(ExperimentalForeignApi::class)
  val iconvHandle = binom_iconv_open(toCharset, fromCharset)

  val inputAvail = nativeHeap.alloc<size_tVar>()
  val outputAvail = nativeHeap.alloc<size_tVar>()
  val outputPointer = nativeHeap.allocPointerTo<CPointerVar<ByteVar>>()
  val inputPointer = nativeHeap.allocPointerTo<CPointerVar<ByteVar>>()

  init {
    set_posix_errno(0)
    val r = binom_iconv(
      iconvHandle,
      null,
      null,
      outputPointer.ptr.reinterpret(),
      outputAvail.ptr,
    ).toInt()
    if (r == -1 && errno == EBADF) {
      throw IllegalArgumentException("Charset not supported")
    }
  }

  fun dispose() {
    binom_iconv_close(iconvHandle)
    nativeHeap.free(inputAvail)
    nativeHeap.free(outputAvail)
    nativeHeap.free(outputPointer)
    nativeHeap.free(inputPointer)
  }
}
