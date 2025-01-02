package pw.binom.charset

import kotlinx.cinterop.*
import platform.iconv.iconv
import platform.iconv.iconv_close
import platform.iconv.iconv_open
import platform.binomiconv.*
import kotlinx.cinterop.*
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
  ): Long = binom_iconv(cd?.reinterpret(), inbuf, inbytesleft, outbuf, outbytesleft).convert()

  actual fun close(__cd: CPointer<out CPointed>?): Int = iconv_close(__cd)
}
