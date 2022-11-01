package pw.binom.charset

import pw.binom.CharBuffer
import pw.binom.io.ByteBuffer

class IncovCharsetEncoder(
    name: String,
    onClose: ((AbstractIconv) -> Unit)?,
) : CharsetEncoder, AbstractIconv(
    fromCharset = NATIVE_CHARSET,
    toCharset = name,
    onClose = onClose
) {

    init {
        CharsetMetrics.incEncoder()
    }

    override fun encode(input: CharBuffer, output: ByteBuffer): CharsetTransformResult =
        iconv(
            input,
            output
        )

    override fun close() {
        super.close()
        CharsetMetrics.decEncoder()
    }
}
