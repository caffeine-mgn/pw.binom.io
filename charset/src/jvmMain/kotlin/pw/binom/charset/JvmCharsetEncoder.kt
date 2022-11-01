package pw.binom.charset

import pw.binom.CharBuffer
import pw.binom.io.ByteBuffer
import pw.binom.io.ClosedException

class JvmCharsetEncoder(val native: java.nio.charset.CharsetEncoder) : CharsetEncoder {

    init {
        CharsetMetrics.incEncoder()
    }

    override fun encode(input: CharBuffer, output: ByteBuffer): CharsetTransformResult {
        val r = native.encode(input.native, output.native, true)
        return when {
            r.isUnderflow -> CharsetTransformResult.SUCCESS
            r.isOverflow -> CharsetTransformResult.OUTPUT_OVER
            else -> TODO("Not yet implemented")
        }
    }

    private var closed = false

    override fun close() {
        if (closed) {
            throw ClosedException()
        }
        try {
            CharsetMetrics.decEncoder()
        } finally {
            closed = true
        }
    }
}
