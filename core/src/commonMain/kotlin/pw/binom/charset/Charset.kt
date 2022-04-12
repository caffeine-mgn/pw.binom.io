package pw.binom.charset

interface Charset {
    val name: String

    /**
     * Creates decoder for [name] charset. After work you should call [CharsetDecoder.close]
     */
    fun newDecoder(): CharsetDecoder

    /**
     * Creates encoder for [name] charset. After work you should call [CharsetEncoder.close]
     */
    fun newEncoder(): CharsetEncoder
}
