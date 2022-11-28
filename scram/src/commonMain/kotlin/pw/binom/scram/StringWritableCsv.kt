package pw.binom.scram

object StringWritableCsv {
    private fun writeStringWritableToStringBuffer(value: StringWritable?, sb: Appendable) {
        value?.writeTo(sb)
    }

    /**
     * Write a sequence of [StringWritableCsv]s to a StringBuffer.
     * Null [StringWritable]s are not printed, but separator is still used.
     * Separator is a comma (',')
     * @param sb The sb to write to
     * @param values Zero or more attribute-value pairs to write
     * @return The same sb, with data filled in (if any)
     * @throws IllegalArgumentException If sb is null
     */
    fun <T : Appendable> writeTo(sb: T, vararg values: StringWritable?): T {
        if (values.isEmpty()) {
            return sb
        }
        writeStringWritableToStringBuffer(values[0], sb)
        var i = 1
        while (i < values.size) {
            sb.append(',')
            writeStringWritableToStringBuffer(values[i], sb)
            i++
        }
        return sb
    }

    /**
     * Parse a String with a [StringWritableCsv] into its composing Strings
     * represented as Strings. No validation is performed on the individual attribute-values returned.
     * @param value The String with the set of attribute-values
     * @param n Number of entries to return (entries will be null of there were not enough). 0 means unlimited
     * @param offset How many entries to skip before start returning
     * @return An array of Strings which represent the individual attribute-values
     * @throws IllegalArgumentException If value is null or either n or offset are negative
     */
    fun parseFrom(value: String, n: Int, offset: Int): Array<String> {
        if (n < 0 || offset < 0) {
            throw IllegalArgumentException("Limit and offset have to be >= 0")
        }
        if (value.isEmpty()) {
            return arrayOf()
        }
        val split = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (split.size < offset) {
            throw IllegalArgumentException("Not enough items for the given offset")
        }
        return split.copyOfRange(offset, offset + (if (n == 0) split.size else n) + offset)
//        return java.util.Arrays.copyOfRange<String>(
//            split,
//            offset,
//            (if (n == 0) split.size else n) + offset
//        )
    }

    /**
     * Parse a String with a [StringWritableCsv] into its composing Strings
     * represented as Strings. No validation is performed on the individual attribute-values returned.
     * Elements are returned starting from the first available attribute-value.
     * @param value The String with the set of attribute-values
     * @param n Number of entries to return (entries will be null of there were not enough). 0 means unlimited
     * @return An array of Strings which represent the individual attribute-values
     * @throws IllegalArgumentException If value is null or n is negative
     */
    fun parseFrom(value: String, n: Int): Array<String> {
        return parseFrom(value = value, n = n, offset = 0)
    }

    /**
     * Parse a String with a [StringWritableCsv] into its composing Strings
     * represented as Strings. No validation is performed on the individual attribute-values returned.
     * All the available attribute-values will be returned.
     * @param value The String with the set of attribute-values
     * @return An array of Strings which represent the individual attribute-values
     * @throws IllegalArgumentException If value is null
     */
    fun parseFrom(value: String): Array<String> {
        return parseFrom(value, 0, 0)
    }
}
