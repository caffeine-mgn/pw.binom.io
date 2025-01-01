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

  actual fun iconv2(
    cd: CPointer<out CPointed>?,
    inbuf: CValuesRef<CPointerVarOf<CPointer<out CPointed>>>?,
    inbytesleft: CValuesRef<LongVarOf<Long>>?,
    outbuf: CValuesRef<CPointerVarOf<CPointer<out CPointed>>>?,
    outbytesleft: CValuesRef<LongVarOf<Long>>?,
  ): Long = binom_iconv(cd?.reinterpret(), inbuf, inbytesleft, outbuf, outbytesleft)

  actual fun close(__cd: CPointer<out CPointed>?): Int = binom_iconv_close(__cd?.reinterpret())
}
