package pw.binom

actual fun ByteArray.asUTF8String(): String =
        String(this, Charsets.UTF_8)

actual fun String.asUTF8ByteArray(): ByteArray = toByteArray(Charsets.UTF_8)