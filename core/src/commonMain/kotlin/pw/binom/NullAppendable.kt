package pw.binom

object NullAppendable : Appendable {
    override fun append(value: Char): Appendable = this

    override fun append(value: CharSequence?): Appendable = this

    override fun append(value: CharSequence?, startIndex: Int, endIndex: Int): Appendable = this
}
