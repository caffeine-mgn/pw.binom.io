package pw.binom

actual fun ByteArray.asUTF8String(startIndex: Int, length: Int): String =
        String(this, startIndex, length, Charsets.UTF_8)

actual fun String.asUTF8ByteArray(): ByteArray = toByteArray(Charsets.UTF_8)