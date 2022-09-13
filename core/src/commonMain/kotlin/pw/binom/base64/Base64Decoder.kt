package pw.binom.base64

import kotlin.experimental.or

class Base64Decoder(val emmitByte: (Byte) -> Unit) {
    private var g = 0
    private var b = 0.toByte()

    companion object {
        fun calcSize(stringLength: Int) = (stringLength + 3) / 4 * 3
    }

    fun add(value: Char) {
        if (value == '=') {
            g++
            return
        }
        val value2 = charFromBase64(value)
        when (g) {
            0 -> {
                b = b or (value2 shl 2)
            }

            1 -> {
                val write = b or (value2 shr 4)
                emmitByte(write)
                b = value2 shl 4
            }

            2 -> {
                val write = b or (value2 shr 2)
                emmitByte(write)
                b = value2 shl 6
            }

            3 -> {
                val write = b or (value2)
                emmitByte(write)
                b = 0
            }

            else -> throw RuntimeException()
        }
        g++
        if (g == 4) {
            g = 0
        }
    }
}
