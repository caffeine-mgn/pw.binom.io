package pw.binom

@UseExperimental(ExperimentalStdlibApi::class)
actual fun ByteArray.asUTF8String(offset: Int, length: Int): String =
        this.decodeToString(startIndex = offset, endIndex = offset + length, throwOnInvalidSequence = true)

@UseExperimental(ExperimentalStdlibApi::class)
actual fun String.asUTF8ByteArray(): ByteArray = this.encodeToByteArray()