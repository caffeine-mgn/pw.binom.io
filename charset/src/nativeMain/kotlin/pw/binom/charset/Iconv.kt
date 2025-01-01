package pw.binom.charset

import kotlinx.cinterop.*
import platform.posix.size_tVar

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
expect object Iconv {
  fun open(tocode: String?, fromcode: String?): CPointer<out CPointed>?

  fun iconv1(
    __cd: CPointer<out CPointed>?,
    __inbuf: CValuesRef<CPointerVarOf<CPointer<ByteVarOf<Byte>>>>?,
    __inbytesleft: CValuesRef<size_tVar>?,
    __outbuf: CValuesRef<CPointerVarOf<CPointer<ByteVarOf<Byte>>>>?,
    __outbytesleft: CValuesRef<size_tVar>?,
  ): ULong

  fun close(__cd: CPointer<out CPointed>?): Int
}
