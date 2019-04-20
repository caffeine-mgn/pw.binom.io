package pw.binom

actual fun ByteArray.asUTF8String(offset: Int, length: Int): String =
        String(this, offset, length, Charsets.UTF_8)

actual fun String.asUTF8ByteArray(): ByteArray = toByteArray(Charsets.UTF_8)