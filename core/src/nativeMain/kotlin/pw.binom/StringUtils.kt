package pw.binom

actual fun ByteArray.asUTF8String(offset: Int, length: Int): String =
        this.stringFromUtf8OrThrow(start = offset, size = length)

actual fun String.asUTF8ByteArray(): ByteArray = this.toUtf8OrThrow()