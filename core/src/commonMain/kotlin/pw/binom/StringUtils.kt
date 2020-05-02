package pw.binom

expect fun ByteArray.asUTF8String(startIndex: Int = 0, length: Int = size - startIndex): String
expect fun String.asUTF8ByteArray(): ByteArray