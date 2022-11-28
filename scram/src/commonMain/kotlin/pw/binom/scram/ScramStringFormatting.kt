package pw.binom.scram

import pw.binom.base64.Base64

/**
 * Class with static methods that provide support for converting to/from salNames.
 * @see <a href="https://tools.ietf.org/html/rfc5802#section-7">[RFC5802] Section 7: Formal Syntax</a>
 */
object ScramStringFormatting {
    /**
     * Given a value-safe-char (normalized UTF-8 String),
     * return one where characters ',' and '=' are represented by '=2C' or '=3D', respectively.
     * @param value The value to convert so saslName
     * @return The saslName, with caracter escaped (if any)
     */
    fun toSaslName(value: String?): String? {
        if (value.isNullOrEmpty()) {
            return value
        }
        var nComma = 0
        var nEqual = 0
        val originalChars = value.toCharArray()

        // Fast path
        for (c in originalChars) {
            if (',' == c) {
                nComma++
            } else if ('=' == c) {
                nEqual++
            }
        }
        if (nComma == 0 && nEqual == 0) {
            return value
        }

        // Replace chars
        val saslChars = CharArray(originalChars.size + nComma * 2 + nEqual * 2)
        var i = 0
        for (c in originalChars) {
            if (',' == c) {
                saslChars[i++] = '='
                saslChars[i++] = '2'
                saslChars[i++] = 'C'
            } else if ('=' == c) {
                saslChars[i++] = '='
                saslChars[i++] = '3'
                saslChars[i++] = 'D'
            } else {
                saslChars[i++] = c
            }
        }
        return saslChars.concatToString()
    }

    /**
     * Given a saslName, return a non-escaped String.
     * @param value The saslName
     * @return The saslName, unescaped
     * @throws IllegalArgumentException If a ',' character is present, or a '=' not followed by either '2C' or '3D'
     */
    fun fromSaslName(value: String?): String? {
        if (null == value || value.isEmpty()) {
            return value
        }
        var nEqual = 0
        val orig = value.toCharArray()

        // Fast path
        for (i in orig.indices) {
            if (orig[i] == ',') {
                throw IllegalArgumentException("Invalid ',' character present in saslName")
            }
            if (orig[i] == '=') {
                nEqual++
                if (i + 2 > orig.size - 1) {
                    throw IllegalArgumentException("Invalid '=' character present in saslName")
                }
                if (!(orig[i + 1] == '2' && orig[i + 2] == 'C' || orig[i + 1] == '3' && orig[i + 2] == 'D')) {
                    throw IllegalArgumentException(
                        "Invalid char '=" + orig[i + 1] + orig[i + 2] + "' found in saslName"
                    )
                }
            }
        }
        if (nEqual == 0) {
            return value
        }

        // Replace characters
        val replaced = CharArray(orig.size - nEqual * 2)
        var r = 0
        var o = 0
        while (r < replaced.size) {
            if ('=' == orig[o]) {
                if (orig[o + 1] == '2' && orig[o + 2] == 'C') {
                    replaced[r] = ','
                } else if (orig[o + 1] == '3' && orig[o + 2] == 'D') {
                    replaced[r] = '='
                }
                o += 3
            } else {
                replaced[r] = orig[o]
                o += 1
            }
            r++
        }
        return replaced.concatToString()
    }

    fun base64Encode(value: ByteArray): String = Base64.encode(value)

    fun base64Encode(value: String): String = Base64.encode(value.encodeToByteArray())

    fun base64Decode(value: String): ByteArray = Base64.decode(value)
}
