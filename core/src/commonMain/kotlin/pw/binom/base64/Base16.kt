package pw.binom.base64

import pw.binom.ByteBuffer

private val table = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

object Base16 {
    internal fun encodeByte(byte: Byte): String =
        "${table[byte.toInt() and 0xf0 ushr 4]}${table[byte.toInt() and 0x0f]}"

    fun encode(data: ByteBuffer): String {
        val sb = StringBuilder(data.remaining * 2)
        while (data.remaining > 0) {
            sb.append(encodeByte(data.get()))
        }
        return sb.toString()
    }

    fun encode(data: ByteArray): String {
        val sb = StringBuilder(data.size * 2)
        data.forEach {
            sb.append(encodeByte(it))
        }
        return sb.toString()
    }
}
