package pw.binom.io

import pw.binom.*

object UTF8 {

    fun unicodeToUtf8(text: String, out: ByteBuffer): Int {
        var len = 0
        text.forEach { char ->
            len += unicodeToUtf8(char, out)
        }
        return len
    }

    fun unicodeToUtf8(char: Char, out: ByteBuffer): Int {
        val utf = char.code
        return when {
            utf <= 0x7F -> {
                // Plain ASCII
                out.put(utf.toByte())
                1
            }
            utf <= 0x07FF -> {
                // 2-byte unicode
                out.put((((utf shr 6) and 0x1F) or 0xC0).toByte())
                out.put((((utf shr 0) and 0x3F) or 0x80).toByte())
                2
            }
            utf <= 0xFFFF -> {
                // 3-byte unicode
                out.put((((utf shr 12) and 0x0F) or 0xE0).toByte())
                out.put((((utf shr 6) and 0x3F) or 0x80).toByte())
                out.put((((utf shr 0) and 0x3F) or 0x80).toByte())
                3
            }
            utf <= 0x10FFFF -> {
                // 4-byte unicode
                out.put((((utf shr 18) and 0x07) or 0xF0).toByte())
                out.put((((utf shr 12) and 0x3F) or 0x80).toByte())
                out.put((((utf shr 6) and 0x3F) or 0x80).toByte())
                out.put((((utf shr 0) and 0x3F) or 0x80).toByte())
                4
            }
            else -> {
                // error - use replacement character
                out.put(0xEF.toByte())
                out.put(0xBF.toByte())
                out.put(0xBD.toByte())
                0
            }
        }
    }

    /**
     * Converts unicode character to utf8 character
     *
     * @param char input character
     * @param out output byte array
     * @return size of full utf8 character in bytes
     */
    fun unicodeToUtf8(char: Char, out: ByteArray): Int {
        val utf = char.code
        return when {
            utf <= 0x7F -> {
                // Plain ASCII
                out[0] = utf.toByte()
                1
            }
            utf <= 0x07FF -> {
                // 2-byte unicode
                out[0] = (((utf shr 6) and 0x1F) or 0xC0).toByte()
                out[1] = (((utf shr 0) and 0x3F) or 0x80).toByte()
                2
            }
            utf <= 0xFFFF -> {
                // 3-byte unicode
                out[0] = (((utf shr 12) and 0x0F) or 0xE0).toByte()
                out[1] = (((utf shr 6) and 0x3F) or 0x80).toByte()
                out[2] = (((utf shr 0) and 0x3F) or 0x80).toByte()
                3
            }
            utf <= 0x10FFFF -> {
                // 4-byte unicode
                out[0] = (((utf shr 18) and 0x07) or 0xF0).toByte()
                out[1] = (((utf shr 12) and 0x3F) or 0x80).toByte()
                out[2] = (((utf shr 6) and 0x3F) or 0x80).toByte()
                out[3] = (((utf shr 0) and 0x3F) or 0x80).toByte()
                4
            }
            else -> {
                // error - use replacement character
                out[0] = 0xEF.toByte()
                out[1] = 0xBF.toByte()
                out[2] = 0xBD.toByte()
                3
            }
        }
    }

    fun unicodeToUtf8Size(char: Char): Int {
        val utf = char.code
        return when {
            utf <= 0x7F -> {
                // Plain ASCII
                1
            }
            utf <= 0x07FF -> {
                // 2-byte unicode
                2
            }
            utf <= 0xFFFF -> {
                // 3-byte unicode
                3
            }
            utf <= 0x10FFFF -> {
                // 4-byte unicode
                4
            }
            else -> {
                // error - use replacement character
                3
            }
        }
    }

    /**
     * Returns size of character by first byte
     *
     * @param first byte of character
     * @return size of full utf8 character
     */
    @Deprecated(message = "Deprecated", replaceWith = ReplaceWith("this.getUtf8CharSize(firstByte)-1"))
    fun utf8CharSize(firstByte: Byte): Int {
        val c = firstByte.toInt() and 0xFF
        return when {
            (c and 0x80) == 0 -> 1 - 1
            (c and 0xE0) == 0xC0 -> 2 - 1
            (c and 0xF0) == 0xE0 -> 3 - 1
            (c and 0xF8) == 0xF0 -> 4 - 1
            (c and 0xFC) == 0xF8 -> 5 - 1
            (c and 0xFE) == 0xFC -> 6 - 1
            else -> throw IllegalArgumentException("Unknown Character 0x${c.toUByte().toString(16)}")
        }
    }

    /**
     * Returns size of character by first byte
     *
     * @param first byte of character
     * @return size of full utf8 character
     */
    fun getUtf8CharSize(firstByte: Byte): Int {
        val c = firstByte.toInt() and 0xFF
        return when {
            (c and 0x80) == 0 -> 1
            (c and 0xE0) == 0xC0 -> 2
            (c and 0xF0) == 0xE0 -> 3
            (c and 0xF8) == 0xF0 -> 4
            (c and 0xFC) == 0xF8 -> 5
            (c and 0xFE) == 0xFC -> 6
            else -> throw IllegalArgumentException("Unknown Character 0x${c.toUByte().toString(16)}")
        }
    }

    /**
     * Converts utf8 character to unicode
     * @param firstByte first byte of utf8 character
     * @param otherBytes other bytes of utf8 character
     * @param offset offset for [otherBytes]
     * @return full unicode character
     */
    fun utf8toUnicode(firstByte: Byte, otherBytes: ByteArray, offset: Int = 0): Char {
        val c = firstByte.toInt()
        var cur = offset
        fun func() = otherBytes[cur++]
        return when {
            (c and 0x80) == 0 -> c
            (c and 0xE0) == 0xC0 -> {
                ((c and 0x1F) shl 6) or (func().toInt() and 0x3F)
            }
            (c and 0xF0) == 0xE0 -> {
                ((c and 0xF) shl 12) or
                    ((func().toInt() and 0x3F) shl 6) or
                    ((func().toInt() and 0x3F))
            }
            (c and 0xF8) == 0xF0 -> {
                ((c and 0x7) shl 18) or
                    ((func().toInt() and 0x3F) shl 12) or
                    ((func().toInt() and 0x3F) shl 6) or
                    ((func().toInt() and 0x3F))
            }
            (c and 0xFC) == 0xF8 -> {
                ((c and 0x3) shl 24) or
                    ((func().toInt() and 0x3F) shl 18) or
                    ((func().toInt() and 0x3F) shl 12) or
                    ((func().toInt() and 0x3F) shl 6) or
                    ((func().toInt() and 0x3F))
            }
            (c and 0xFE) == 0xFC -> {
                ((c and 0x1) shl 30) or
                    ((func().toInt() and 0x3F) shl 24) or
                    ((func().toInt() and 0x3F) shl 18) or
                    ((func().toInt() and 0x3F) shl 12) or
                    ((func().toInt() and 0x3F) shl 6) or
                    ((func().toInt() and 0x3F))
            }
            else -> throw IllegalArgumentException("Unknown Character #$c")
        }.toChar()
    }

    fun utf8toUnicode(b1: Int): Char {
        return when {
            (b1 and 0x80) == 0 -> b1
            else -> throw IllegalArgumentException("Unknown Character #$b1")
        }.toChar()
    }

    fun utf8toUnicode(b1: Int, b2: Int): Char {
        return when {
            (b1 and 0xE0) == 0xC0 -> {
                ((b1 and 0x1F) shl 6) or (b2 and 0x3F)
            }
            else -> throw IllegalArgumentException("Unknown Character #$b1")
        }.toChar()
    }

    fun utf8toUnicode(b1: Int, b2: Int, b3: Int): Char {
        return when {
            (b1 and 0xF0) == 0xE0 -> {
                ((b1 and 0xF) shl 12) or
                    ((b2 and 0x3F) shl 6) or
                    ((b3 and 0x3F))
            }
            else -> throw IllegalArgumentException("Unknown Character #$b1")
        }.toChar()
    }

    fun utf8toUnicode(b1: Int, b2: Int, b3: Int, b4: Int): Char {
        return when {
            (b1 and 0xF8) == 0xF0 -> {
                ((b1 and 0x7) shl 18) or
                    ((b2 and 0x3F) shl 12) or
                    ((b3 and 0x3F) shl 6) or
                    ((b4 and 0x3F))
            }
            else -> throw IllegalArgumentException("Unknown Character #$b1")
        }.toChar()
    }

    fun utf8toUnicode(b1: Int, b2: Int, b3: Int, b4: Int, b5: Int): Char {
        return when {
            (b1 and 0xFC) == 0xF8 -> {
                ((b1 and 0x3) shl 24) or
                    ((b2 and 0x3F) shl 18) or
                    ((b3 and 0x3F) shl 12) or
                    ((b4 and 0x3F) shl 6) or
                    ((b5 and 0x3F))
            }
            else -> throw IllegalArgumentException("Unknown Character #$b1")
        }.toChar()
    }

    fun utf8toUnicode(b1: Int, b2: Int, b3: Int, b4: Int, b5: Int, b6: Int): Char {
        return when {
            (b1 and 0xFE) == 0xFC -> {
                ((b1 and 0x1) shl 30) or
                    ((b2 and 0x3F) shl 24) or
                    ((b3 and 0x3F) shl 18) or
                    ((b4 and 0x3F) shl 12) or
                    ((b5 and 0x3F) shl 6) or
                    ((b6 and 0x3F))
            }
            else -> throw IllegalArgumentException("Unknown Character #$b1")
        }.toChar()
    }

    fun utf8toUnicode(firstByte: Byte, otherBytes: ByteBuffer): Char {
        val c = firstByte.toInt()
        fun func() = otherBytes.get()
        return when {
            (c and 0x80) == 0 -> c
            (c and 0xE0) == 0xC0 -> {
                ((c and 0x1F) shl 6) or (func().toInt() and 0x3F)
            }
            (c and 0xF0) == 0xE0 -> {
                ((c and 0xF) shl 12) or
                    ((func().toInt() and 0x3F) shl 6) or
                    ((func().toInt() and 0x3F))
            }
            (c and 0xF8) == 0xF0 -> {
                ((c and 0x7) shl 18) or
                    ((func().toInt() and 0x3F) shl 12) or
                    ((func().toInt() and 0x3F) shl 6) or
                    ((func().toInt() and 0x3F))
            }
            (c and 0xFC) == 0xF8 -> {
                ((c and 0x3) shl 24) or
                    ((func().toInt() and 0x3F) shl 18) or
                    ((func().toInt() and 0x3F) shl 12) or
                    ((func().toInt() and 0x3F) shl 6) or
                    ((func().toInt() and 0x3F))
            }
            (c and 0xFE) == 0xFC -> {
                ((c and 0x1) shl 30) or
                    ((func().toInt() and 0x3F) shl 24) or
                    ((func().toInt() and 0x3F) shl 18) or
                    ((func().toInt() and 0x3F) shl 12) or
                    ((func().toInt() and 0x3F) shl 6) or
                    ((func().toInt() and 0x3F))
            }
            else -> throw IllegalArgumentException("Unknown Character #$c")
        }.toChar()
    }

    fun encode(input: String): String {
        val sb = StringBuilder(input.length)
        input.encodeToByteArray().forEach {
            when (it) {
                '.'.code.toByte(),
                '-'.code.toByte(),
                '_'.code.toByte(),
                '*'.code.toByte(),
                in 'a'.code.toByte()..'z'.code.toByte(),
                in 'A'.code.toByte()..'Z'.code.toByte(),
                in '0'.code.toByte()..'9'.code.toByte()
                -> sb.append(it.toInt().toChar())
                else -> {
                    val bb1 = ((it.toInt() and 0xf0) shr 4)
                    val bb2 = it.toInt() and 0x0f
                    sb.append("%").append(bb1.toString(16)).append(bb2.toString(16))
                }
            }
        }
        return sb.toString()
    }

    fun decode(input: String): String {
        if (input.isEmpty()) {
            return ""
        }
        val outputBuffer = ByteArray(input.length)
        var index = 0
        var charCounter = 0
        while (index < input.length) {
            if (input[index] == '%') {
                val b1 = (input[++index].toString().toInt(16) and 0xf) shl 4
                val b2 = input[++index].toString().toInt(16) and 0xf
                outputBuffer[charCounter++] = (b1 + b2).toByte()
            } else {
                outputBuffer[charCounter++] = input[index].code.toByte()
            }
            index++
        }
        return outputBuffer.decodeToString(endIndex = charCounter)
    }

    fun urlEncode(url: String) =
        url.splitToSequence("/").map { encode(it) }.joinToString("/")

    fun urlDecode(url: String) =
        url.splitToSequence("/").map { decode(it) }.joinToString("/")
}
