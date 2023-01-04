package pw.binom.url

actual object UrlEncoder {
    actual fun encode(input: String): String {
        if (input.isEmpty()) {
            return input
        }
        val sb = StringBuilder(input.length)
        input.encodeToByteArray().forEach {
            when (it) {
                '.'.code.toByte(),
                '-'.code.toByte(),
                '_'.code.toByte(),
                '~'.code.toByte(),
                '*'.code.toByte(),
                in 'a'.code.toByte()..'z'.code.toByte(),
                in 'A'.code.toByte()..'Z'.code.toByte(),
                in '0'.code.toByte()..'9'.code.toByte()
                -> sb.append(it.toInt().toChar())

                else -> {
                    val bb1 = ((it.toInt() and 0xf0) shr 4)
                    val bb2 = it.toInt() and 0x0f
                    sb.append("%").append(bb1.toString(16).uppercase()).append(bb2.toString(16).uppercase())
                }
            }
        }
        return sb.toString()
    }

    actual fun decode(input: String): String {
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

    actual fun pathEncode(input: String): String = input.splitToSequence("/").map { encode(it) }.joinToString("/")

    actual fun pathDecode(input: String): String = input.splitToSequence("/").map { decode(it) }.joinToString("/")
}
