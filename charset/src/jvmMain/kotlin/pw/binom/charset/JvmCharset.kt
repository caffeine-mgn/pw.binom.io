package pw.binom.charset

class JvmCharset(val native: java.nio.charset.Charset) : Charset {
    override val name: String
        get() = native.name()

    override fun newDecoder(): CharsetDecoder =
        JvmCharsetDecoder(native.newDecoder())

    override fun newEncoder(): CharsetEncoder =
        JvmCharsetEncoder(native.newEncoder())
}
