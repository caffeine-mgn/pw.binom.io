package pw.binom

expect fun ByteArray.asUTF8String(offset: Int = 0, length: Int = size - offset): String
expect fun String.asUTF8ByteArray(): ByteArray