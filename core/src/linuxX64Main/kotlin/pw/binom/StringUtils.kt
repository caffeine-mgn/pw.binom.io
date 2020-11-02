package pw.binom

import pw.binom.charset.Charset
import pw.binom.charset.CharsetTransformResult
import pw.binom.io.ByteArrayOutput

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

actual fun ByteArray.decodeString(charset: Charset, offset: Int, length: Int): String {
    val self = ByteBuffer.wrap(
        data = this,
        offset = offset,
        length = length
    )
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
        self.close()
        decoder.close()
    }
}