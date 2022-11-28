package pw.binom.scram

class ScramAttributeValue(attribute: ScramAttributes, value: String) :
    AbstractCharAttributeValue(attribute, value) {

    companion object {
        fun writeTo(sb: Appendable, attribute: ScramAttributes, value: String): Appendable {
            return ScramAttributeValue(attribute, value).writeTo(sb)
        }

        /**
         * Parses a potential ScramAttributeValue String.
         * @param value The string that contains the Attribute-Value pair.
         * @return The parsed class
         * @throws ScramParseException If the argument is empty or an invalid Attribute-Value
         */
        @Throws(ScramParseException::class)
        fun parse(value: String?): ScramAttributeValue {
            if (value == null || value.length < 3 || value[1] != '=') {
                throw ScramParseException("Invalid ScramAttributeValue '$value'")
            }
            return ScramAttributeValue(ScramAttributes.byChar(value[0]), value.substring(2))
        }
    }
}
