package pw.binom

import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.use

object Base64 {
    fun encode(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): String {
        val sb = StringBuilder()
        Base64EncodeOutputStream(sb).use {
            it.write(data = data, offset = offset, length = length)
        }
        return sb.toString()
    }

    fun decode(data: String, offset: Int = 0, length: Int = data.length - offset): ByteArray {
        val out = ByteArrayOutputStream()
        val o = Base64DecodeAppendable(out)
        o.append(data, offset, offset + length)
        return out.toByteArray()
    }
}

internal infix fun Byte.shl(count: Int) = ((toInt() and 0xFF) shl count).toByte()
internal infix fun Byte.shr(count: Int) = ((toInt() and 0xFF) shr count).toByte()