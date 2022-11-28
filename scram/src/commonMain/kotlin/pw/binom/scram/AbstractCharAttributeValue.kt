package pw.binom.scram

open class AbstractCharAttributeValue(
    private val charAttribute: CharAttribute,
    private val value: String?,
) : AbstractStringWritable(), CharAttributeValue {
    init {
        value?.let { require(it.isNotEmpty()) { "Value should be either null or non-empty" } }
    }

    override fun getValue(): String? = value

    override fun getChar(): Char = charAttribute.getChar()

    override fun writeTo(sb: Appendable): Appendable {
        sb.append(charAttribute.getChar())

        if (null != value) {
            sb.append('=').append(value)
        }

        return sb
    }
}
