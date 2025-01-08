package pw.binom.charset

import kotlinx.cinterop.*
import platform.binomiconv.binom_iconv
import platform.iconv.iconv
import platform.iconv.iconv_close
import platform.iconv.iconv_open
import platform.posix.size_tVar


@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual object Iconv {
  actual fun open(tocode: String?, fromcode: String?): CPointer<out CPointed>? =
    iconv_open(tocode, fromcode)

  actual fun iconv2(
    cd: CPointer<out CPointed>?,
    inbuf: CPointer<COpaquePointerVar>?,
    inbytesleft: CValuesRef<LongVarOf<Long>>?,
    outbuf: CPointer<COpaquePointerVar>?,
    outbytesleft: CValuesRef<LongVarOf<Long>>?,
  ): Long {
    return binom_iconv(cd, inbuf, inbytesleft, outbuf, outbytesleft)
  }

//  actual fun iconv1(
//    __cd: CPointer<out CPointed>?,
//    __inbuf: CValuesRef<CPointerVarOf<CPointer<ByteVarOf<Byte>>>>?,
//    __inbytesleft: CValuesRef<size_tVar>?,
//    __outbuf: CValuesRef<CPointerVarOf<CPointer<ByteVarOf<Byte>>>>?,
//    __outbytesleft: CValuesRef<size_tVar>?,
//  ): ULong = binom_iconv(__cd, __inbuf, __inbytesleft, __outbuf, __outbytesleft)

  actual fun close(__cd: CPointer<out CPointed>?): Int = iconv_close(__cd)
}
