package pw.binom.charset

import kotlinx.cinterop.*
import platform.posix.size_tVar

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
expect object Iconv {
  fun open(tocode: String?, fromcode: String?): CPointer<out CPointed>?
  fun iconv2(
  cd: CPointer<out CPointed>?,
  inbuf: CValuesRef<CPointerVarOf<CPointer<out CPointed>>>?,
  inbytesleft: CValuesRef<LongVarOf<Long>>?,
  outbuf: CValuesRef<CPointerVarOf<CPointer<out CPointed>>>?,
  outbytesleft: CValuesRef<LongVarOf<Long>>?):Long
//  fun iconv1(
//    __cd: CPointer<out CPointed>?,
//    __inbuf: CPointerVar<CPointerVar<ByteVar>>?,
//    __inbytesleft: CValuesRef<size_tVar>?,
//    __outbuf: CPointerVar<CPointerVar<ByteVar>>?,
//    __outbytesleft: CValuesRef<size_tVar>?,
//  ): ULong

  fun close(__cd: CPointer<out CPointed>?): Int
}
