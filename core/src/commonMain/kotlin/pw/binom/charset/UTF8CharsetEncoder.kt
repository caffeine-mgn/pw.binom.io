package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import pw.binom.io.UTF8

class UTF8CharsetEncoder : CharsetEncoder {
    override fun encode(input: CharBuffer, output: ByteBuffer): CharsetTransformResult {
        while (true) {
            if (input.remaining == 0) {
                return CharsetTransformResult.SUCCESS
            }
            val size = UTF8.unicodeToUtf8Size(input.peek())
            if (output.remaining < size) {
                return CharsetTransformResult.OUTPUT_OVER
            }
            UTF8.unicodeToUtf8(input.get(), output)
        }
    }

    override fun close() {
        // Do nothing
    }
}
