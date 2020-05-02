package pw.binom

@UseExperimental(ExperimentalStdlibApi::class)
actual fun ByteArray.asUTF8String(startIndex: Int, length: Int): String =
        this.decodeToString(startIndex = startIndex, endIndex = startIndex + length, throwOnInvalidSequence = true)

@UseExperimental(ExperimentalStdlibApi::class)
actual fun String.asUTF8ByteArray(): ByteArray = this.encodeToByteArray()