package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import java.nio.charset.CharsetEncoder as JCharsetEncoder

class JvmCharsetEncoder(val native: JCharsetEncoder) : CharsetEncoder {

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
