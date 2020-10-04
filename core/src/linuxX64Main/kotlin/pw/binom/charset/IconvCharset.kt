package pw.binom.charset

class IconvCharset(override val name: String) : Charset {
    override fun newDecoder(): CharsetDecoder =
            IconvCharsetDecoder(name)

    override fun newEncoder(): CharsetEncoder =
            IncovCharsetEncoder(name)

}