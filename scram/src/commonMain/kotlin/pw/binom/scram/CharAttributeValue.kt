package pw.binom.scram

/**
 * Augments a {@link CharAttribute} with a String value and the method(s) to write its data to a StringBuffer.
 */
interface CharAttributeValue : CharAttribute, StringWritable {
    /**
     * Returns the value associated with the {@link CharAttribute}
     * @return The String value or null if no value is associated
     */
    fun getValue(): String?
}
