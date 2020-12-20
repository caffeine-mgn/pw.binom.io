package pw.binom.charset

import java.nio.charset.Charset as JCharset

class JvmCharset(val native: JCharset) : Charset {
    override val name: String
        get() = native.name()

    override fun newDecoder(): CharsetDecoder =
            JvmCharsetDecoder(native.newDecoder())

    override fun newEncoder(): CharsetEncoder=
            JvmCharsetEncoder(native.newEncoder())

}