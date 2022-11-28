package pw.binom.scram

/**
 * Represents an attribute (a key name) that is represented by a single char.
 */
interface CharAttribute {
    /**
     * Return the char used to represent this attribute
     * @return The character of the attribute
     */
    fun getChar(): Char
}
