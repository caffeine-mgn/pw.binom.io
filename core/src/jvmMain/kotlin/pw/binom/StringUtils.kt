package pw.binom

import pw.binom.charset.Charset
import pw.binom.charset.JvmCharset

actual fun ByteArray.decodeString(charset: Charset, offset: Int, length: Int): String =
    String(this, offset, length, (charset as JvmCharset).native)

actual fun String.encodeBytes(charset: pw.binom.charset.Charset): ByteArray =
    this.toByteArray((charset as JvmCharset).native)