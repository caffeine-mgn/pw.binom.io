package pw.binom.charset

import kotlinx.cinterop.*

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
expect object Iconv {
  fun open(tocode: String?, fromcode: String?): CPointer<out CPointed>?
  fun iconv2(
    cd: CPointer<out CPointed>?,
    inbuf: CPointer<COpaquePointerVar>?,
    inbytesleft: CValuesRef<LongVarOf<Long>>?,
    outbuf: CPointer<COpaquePointerVar>?,
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
