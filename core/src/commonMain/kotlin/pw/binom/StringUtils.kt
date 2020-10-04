package pw.binom

import pw.binom.charset.Charset

expect fun ByteArray.asUTF8String(startIndex: Int = 0, length: Int = size - startIndex): String
expect fun String.asUTF8ByteArray(): ByteArray
expect fun ByteArray.decodeString(charset:Charset):String
expect fun String.encodeBytes(charset:Charset):ByteArray