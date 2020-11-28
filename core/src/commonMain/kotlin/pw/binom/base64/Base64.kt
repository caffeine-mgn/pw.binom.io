package pw.binom.base64

import pw.binom.ByteBuffer
import pw.binom.empty
import pw.binom.io.ByteArrayOutput
import pw.binom.io.use
import pw.binom.writeByte

object Base64 {

    fun encode(data: ByteArray): String {
        val sb = StringBuilder()
        val buf = ByteBuffer.alloc(1)
        try {
            Base64EncodeOutput(sb).use {
                data.forEach { b ->
                    it.writeByte(buf, b)
                }
            }
            return sb.toString()
        } finally {
            buf.close()
        }
    }

    fun encode(data: ByteBuffer): String {
        val sb = StringBuilder()
        Base64EncodeOutput(sb).use {
            it.write(data = data)
        }
        return sb.toString()
    }

    fun decode(data: String, offset: Int = 0, length: Int = data.length - offset): ByteArray {
        val out = ByteArrayOutput()
        val o = Base64DecodeAppendable(out)
        o.append(data, offset, offset + length)
        out.trimToSize()
        out.data.clear()
        val byteArray = out.data.toByteArray()
        out.close()
        return byteArray
    }
}

internal infix fun Byte.shl(count: Int) = ((toInt() and 0xFF) shl count).toByte()
internal infix fun Byte.shr(count: Int) = ((toInt() and 0xFF) shr count).toByte()