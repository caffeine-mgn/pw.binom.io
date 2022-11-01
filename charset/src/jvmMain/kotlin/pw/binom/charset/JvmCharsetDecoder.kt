package pw.binom.charset

import pw.binom.CharBuffer
import pw.binom.io.ByteBuffer
import pw.binom.io.ClosedException

class JvmCharsetDecoder(val native: java.nio.charset.CharsetDecoder) : CharsetDecoder {

    init {
        CharsetMetrics.incDecoder()
    }

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

    private var closed = false
    override fun close() {
        if (closed) {
            throw ClosedException()
        }
        try {
            CharsetMetrics.decDecoder()
        } finally {
            closed = true
        }
    }
}
