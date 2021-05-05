package pw.binom.base64

import pw.binom.ByteBuffer
import pw.binom.alloc
import pw.binom.io.ByteArrayOutput
import pw.binom.io.use
import pw.binom.writeByte
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.ceil
import kotlin.math.roundToInt

object Base64 {

    internal fun encodeByte(counter: Int, old: Byte, data: Byte, newOld: (Byte) -> Unit): String =
        when (counter) {
            0 -> {
                val ff = data shr 2
                newOld((data and 3) shl 4)
                byteToBase64(ff).toString()
            }
            1 -> {
                val ff = old or (data shr 4)
                newOld((data and 15) shl 2)
                byteToBase64(ff).toString()
            }
            2 -> {
                val ff = old or (data shr 6)
                newOld(0)
                "${byteToBase64(ff)}${byteToBase64((data and 63))}"
            }
            else -> throw IllegalArgumentException("Argument counter shoul be between 0 and 2")
        }

    fun encode(data: ByteArray, offset: Int = 0, length: Int = data.size - offset): String {
        val sb = StringBuilder((length*1.5).roundToInt())
        var counter = 0
        var old = 0.toByte()
        for (i in offset until offset+length) {
            sb.append(encodeByte(counter, old, data[i]) { old = it })
            counter++
        }
        when (counter) {
            1 -> sb.append(byteToBase64(old)).append("==")
            2 -> sb.append(byteToBase64(old)).append("=")
        }
        return sb.toString()
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
        Base64DecodeAppendable(out).use { o ->
            o.append(data, offset, offset + length)
        }
        out.trimToSize()
        out.data.clear()
        val byteArray = out.data.toByteArray()
        out.close()
        return byteArray
    }
}

internal infix fun Byte.shl(count: Int) = ((toInt() and 0xFF) shl count).toByte()
internal infix fun Byte.shr(count: Int) = ((toInt() and 0xFF) shr count).toByte()