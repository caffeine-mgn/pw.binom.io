package pw.binom.charset

import pw.binom.ByteBuffer
import pw.binom.CharBuffer
import pw.binom.isBigEndian

val NATIVE_CHARSET = if (pw.binom.Environment.isBigEndian) "UCS-2BE" else "UCS-2LE"
class IconvCharsetDecoder(name: String) : CharsetDecoder, AbstractIconv(fromCharset = name, toCharset = NATIVE_CHARSET) {


    override fun decode(input: ByteBuffer, output: CharBuffer): CharsetTransformResult =
            iconv(
                    input,
                    output
            )
}