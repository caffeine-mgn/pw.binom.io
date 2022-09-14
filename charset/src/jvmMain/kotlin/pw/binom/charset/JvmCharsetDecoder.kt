package pw.binom.charset

import pw.binom.CharBuffer
import pw.binom.io.ByteBuffer

class JvmCharsetDecoder(val native: java.nio.charset.CharsetDecoder) : CharsetDecoder {

    override fun decode(input: ByteBuffer, output: CharBuffer): CharsetTransformResult {
        val r = native.decode(input.native, output.native, false)
        return when {
            r.isUnderflow -> CharsetTransformResult.SUCCESS
            r.isOverflow -> CharsetTransformResult.OUTPUT_OVER
            r.isMalformed -> CharsetTransformResult.MALFORMED
            r.isError -> CharsetTransformResult.ERROR
            r.isUnmappable -> CharsetTransformResult.UNMAPPABLE
            else -> TODO("Invalid state of decoder")
        }
    }

    override fun close() {
        // Do nothing
    }
}
