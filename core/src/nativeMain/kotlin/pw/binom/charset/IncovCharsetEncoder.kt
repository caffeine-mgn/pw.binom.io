package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer

class IncovCharsetEncoder(
    name: String,
    onClose: ((AbstractIconv) -> Unit)?,
) : CharsetEncoder, AbstractIconv(
    fromCharset = NATIVE_CHARSET,
    toCharset = name,
    onClose = onClose
) {
    override fun encode(input: CharBuffer, output: ByteBuffer): CharsetTransformResult =
        iconv(
            input,
            output
        )
}
