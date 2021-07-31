package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import java.nio.charset.CharsetDecoder as JCharsetDecoder

class JvmCharsetDecoder(val native: JCharsetDecoder) : CharsetDecoder {

    override fun decode(input: ByteBuffer, output: CharBuffer): CharsetTransformResult {
        val r = native.decode(input.native, output.native, false)
        return when {
            r.isUnderflow -> CharsetTransformResult.SUCCESS
            r.isOverflow -> CharsetTransformResult.OUTPUT_OVER
            r.isMalformed->CharsetTransformResult.MALFORMED
            else->TODO("Invalid state of decoder")
        }
    }
}