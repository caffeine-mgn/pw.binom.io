package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import java.nio.charset.CharsetDecoder as JCharsetDecoder

class JvmCharsetDecoder(val native: JCharsetDecoder) : CharsetDecoder {

    override fun decode(input: ByteBuffer, output: CharBuffer): CharsetTransformResult {
        val r = native.decode(input.native, output.native, true)
        when {
            r.isUnderflow -> return CharsetTransformResult.SUCCESS
            r.isOverflow -> {
                return CharsetTransformResult.OUTPUT_OVER
            }
        }
        TODO("Обработать ошибку не недостаточности входных данных")
    }

    override fun close() {
    }

}