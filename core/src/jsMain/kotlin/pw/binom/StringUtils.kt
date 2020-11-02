package pw.binom

import pw.binom.charset.Charset
import pw.binom.charset.Charsets
import pw.binom.io.ByteArrayOutput
import pw.binom.io.UTF8
import pw.binom.io.use

actual fun ByteArray.decodeString(charset: Charset, offset: Int, length: Int): String {
    if (charset == Charsets.UTF8) {
        return decodeToString(
            startIndex = offset,
            endIndex = offset + length,
            throwOnInvalidSequence = true
        )
    }
    throw IllegalArgumentException("Js not supported decoding from $charset")
}

actual fun String.encodeBytes(charset: Charset): ByteArray {
    if (charset == Charsets.UTF8) {
        return encodeToByteArray()
    }
    throw IllegalArgumentException("Js not supported decoding from $charset")
}