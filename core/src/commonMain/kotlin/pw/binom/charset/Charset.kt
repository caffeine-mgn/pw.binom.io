package pw.binom.charset

interface Charset {
    val name: String
    fun newDecoder(): CharsetDecoder
    fun newEncoder(): CharsetEncoder
}