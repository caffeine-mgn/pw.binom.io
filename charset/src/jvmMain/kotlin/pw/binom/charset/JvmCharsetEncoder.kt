package pw.binom.charset

import pw.binom.CharBuffer
import pw.binom.io.ByteBuffer

class JvmCharsetEncoder(val native: java.nio.charset.CharsetEncoder) : CharsetEncoder {

    override fun encode(input: CharBuffer, output: ByteBuffer): CharsetTransformResult {
        val r = native.encode(input.native, output.native, true)
        return when {
            r.isUnderflow -> CharsetTransformResult.SUCCESS
            r.isOverflow -> CharsetTransformResult.OUTPUT_OVER
            else -> TODO("Not yet implemented")
        }
    }

    override fun close() {
        // Do nothing
    }
}
