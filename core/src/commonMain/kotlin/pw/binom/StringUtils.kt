package pw.binom

import pw.binom.charset.Charset
import pw.binom.charset.Charsets

expect fun ByteArray.decodeString(
    charset: Charset = Charsets.UTF8,
    offset: Int = 0,
    length: Int = size - offset
): String

expect fun String.encodeBytes(charset: Charset = Charsets.UTF8): ByteArray