package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer

class IconvCharsetDecoder(name: String) : CharsetDecoder, AbstractIconv(fromCharset = name, toCharset = NATIVE_CHARSET) {


    override fun decode(input: ByteBuffer, output: CharBuffer): CharsetTransformResult =
            iconv(
                    input,
                    output
            )
}