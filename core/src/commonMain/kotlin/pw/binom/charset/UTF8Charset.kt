package pw.binom.charset

object UTF8Charset : Charset {
    override val name: String
        get() = "UTF-8"

    override fun newDecoder(): CharsetDecoder =
            UTF8CharsetDecoder()

    override fun newEncoder(): CharsetEncoder =
            UTF8CharsetEncoder()
}