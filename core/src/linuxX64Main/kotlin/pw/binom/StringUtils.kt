package pw.binom

import pw.binom.charset.Charset
import pw.binom.charset.CharsetTransformResult
import pw.binom.io.ByteArrayOutput

@OptIn(ExperimentalStdlibApi::class)
actual fun ByteArray.asUTF8String(startIndex: Int, length: Int): String =
        this.decodeToString(startIndex = startIndex, endIndex = startIndex + length, throwOnInvalidSequence = true)

@OptIn(ExperimentalStdlibApi::class)
actual fun String.asUTF8ByteArray(): ByteArray = this.encodeToByteArray()

actual fun String.encodeBytes(charset: Charset): ByteArray {
    val buffer = toCharArray().toCharBuffer()
    val out = ByteArrayOutput(length)
    val encoder = charset.newEncoder()

    while (true) {
        when (encoder.encode(buffer, out.data)) {
            CharsetTransformResult.SUCCESS -> {
                out.data.flip()
                val result = out.data.toByteArray()
                out.close()
                return result
            }
            CharsetTransformResult.OUTPUT_OVER -> {
                out.alloc(8)
                continue
            }
            CharsetTransformResult.MALFORMED, CharsetTransformResult.INPUT_OVER -> throw IllegalArgumentException("Invalid Input String")
        }
    }
}

actual fun ByteArray.decodeString(charset: Charset): String {
    val self = ByteBuffer.wrap(this)
    var out = CharBuffer.alloc(size)
    val decoder = charset.newDecoder()

    try {
        while (true) {
            when (decoder.decode(self, out)) {
                CharsetTransformResult.SUCCESS -> {
                    out.flip()
                    return out.toString()
                }
                CharsetTransformResult.OUTPUT_OVER -> {
                    out = out.realloc(out.capacity + 8)
                    continue
                }
                CharsetTransformResult.MALFORMED, CharsetTransformResult.INPUT_OVER -> throw IllegalArgumentException("Invalid Input String")
            }
        }
    } finally {
        decoder.close()
    }
}