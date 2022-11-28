package pw.binom.scram

/**
 * Interface for all possible String Preparations mechanisms.
 */
interface StringPreparation {
    /**
     * Normalize a UTF-8 String according to this String Preparation rules.
     * @param value The String to prepare
     * @return The prepared String
     * @throws IllegalArgumentException If the String to prepare is not valid.
     */
    fun normalize(value: String?): String?
}
