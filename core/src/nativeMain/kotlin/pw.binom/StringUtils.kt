package pw.binom

@UseExperimental(ExperimentalStdlibApi::class)
actual fun ByteArray.asUTF8String(): String =
        this.decodeToString(startIndex = 0, endIndex = size, throwOnInvalidSequence = true)

@UseExperimental(ExperimentalStdlibApi::class)
actual fun String.asUTF8ByteArray(): ByteArray = this.encodeToByteArray()