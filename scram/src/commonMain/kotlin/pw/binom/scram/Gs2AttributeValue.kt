package pw.binom.scram

/**
 * Parse and write GS2 Attribute-Value pairs.
 */
class Gs2AttributeValue(attribute: Gs2Attributes, value: String?) : AbstractCharAttributeValue(attribute, value) {
    companion object {
        /**
         * Parses a potential Gs2AttributeValue String.
         * @param value The string that contains the Attribute-Value pair (where value is optional).
         * @return The parsed class, or null if the String was null.
         * @throws IllegalArgumentException If the String is an invalid Gs2AttributeValue
         */
        fun parse(value: String): Gs2AttributeValue {
            if (value.isEmpty() || value.length == 2 || value.length > 2 && value[1] != '=') {
                throw IllegalArgumentException("Invalid Gs2AttributeValue")
            }
            return Gs2AttributeValue(
                Gs2Attributes.byChar(value[0]),
                if (value.length > 2) value.substring(2) else null
            )
        }
    }
}
