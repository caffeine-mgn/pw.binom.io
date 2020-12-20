package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import java.nio.charset.CharsetEncoder as JCharsetEncoder

class JvmCharsetEncoder(val native: JCharsetEncoder) : CharsetEncoder {

    override fun encode(input: CharBuffer, output: ByteBuffer): CharsetTransformResult {
        val r = native.encode(input.native, output.native, true)
        when {
            r.isUnderflow -> return CharsetTransformResult.SUCCESS
            r.isOverflow -> {

                return CharsetTransformResult.OUTPUT_OVER
            }
        }
        TODO("Not yet implemented")
    }

    override fun close() {
    }

}