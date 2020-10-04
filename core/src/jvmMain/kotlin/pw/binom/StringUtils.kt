package pw.binom

import pw.binom.charset.JvmCharset

actual fun ByteArray.asUTF8String(startIndex: Int, length: Int): String =
        String(this, startIndex, length, Charsets.UTF_8)

actual fun String.asUTF8ByteArray(): ByteArray = toByteArray(Charsets.UTF_8)

actual fun ByteArray.decodeString(charset: pw.binom.charset.Charset): String =
        String(this, (charset as JvmCharset).native)

actual fun String.encodeBytes(charset: pw.binom.charset.Charset): ByteArray =
        this.toByteArray((charset as JvmCharset).native)