package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import pw.binom.io.UTF8

class UTF8CharsetDecoder : CharsetDecoder {
    override fun decode(input: ByteBuffer, output: CharBuffer): CharsetTransformResult {
        val inputState = input.position to input.limit
        val outputState = output.position to output.limit
        while (true) {
            if (input.remaining == 0) {
                return CharsetTransformResult.SUCCESS
            }
            try {
                val size = UTF8.utf8CharSize(input.peek())
                if (input.remaining < size + 1) {
                    return CharsetTransformResult.INPUT_OVER
                }
                if (output.remaining == 0) {
                    return CharsetTransformResult.OUTPUT_OVER
                }
                output.put(UTF8.utf8toUnicode(input.get(), input))
            } catch (e: Throwable) {
                input.reset(inputState.first, inputState.second)
                output.reset(outputState.first, outputState.second)
                return CharsetTransformResult.MALFORMED
            }
        }
    }
}