package pw.binom.charset

import kotlinx.cinterop.*
import platform.binomiconv.binom_iconv
import platform.binomiconv.binom_iconv_close
import platform.binomiconv.binom_iconv_open
import platform.posix.size_tVar


@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
actual object Iconv {
  actual fun open(tocode: String?, fromcode: String?): CPointer<out CPointed>? =
    binom_iconv_open(tocode, fromcode)

  actual fun iconv1(
    __cd: CPointer<out CPointed>?,
    __inbuf: CValuesRef<CPointerVarOf<CPointer<ByteVarOf<Byte>>>>?,
    __inbytesleft: CValuesRef<size_tVar>?,
    __outbuf: CValuesRef<CPointerVarOf<CPointer<ByteVarOf<Byte>>>>?,
    __outbytesleft: CValuesRef<size_tVar>?,
  ): ULong = binom_iconv(__cd?.reinterpret(), __inbuf, __inbytesleft, __outbuf, __outbytesleft).convert()

  actual fun close(__cd: CPointer<out CPointed>?): Int = binom_iconv_close(__cd?.reinterpret())
}
