package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer

class IncovCharsetEncoder(name: String) : CharsetEncoder, AbstractIconv(fromCharset = NATIVE_CHARSET, toCharset = name) {
    override fun encode(input: CharBuffer, output: ByteBuffer): CharsetTransformResult =
            iconv(
                    input,
                    output
            )
}