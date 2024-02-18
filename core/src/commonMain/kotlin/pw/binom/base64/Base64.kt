package pw.binom.base64

import pw.binom.io.ByteArrayOutput
import pw.binom.io.ByteBuffer
import pw.binom.io.use
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.roundToInt

object Base64 {

  internal inline fun encodeByte(counter: Int, old: Byte, data: Byte, newOld: (Byte) -> Unit): String =
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

      else -> throw IllegalArgumentException("Argument counter should be between 0 and 2. Got $counter")
    }

  fun encode(data: ByteArray, offset: Int = 0, length: Int = data.size - offset, padding: Boolean = true): String {
    val sb = StringBuilder((length * 1.5).roundToInt())
    var counter = 0
    var old = 0.toByte()
    for (i in offset until offset + length) {
      sb.append(encodeByte(counter = counter, old = old, data = data[i]) { old = it })
      counter++
      if (counter == 3) {
        counter = 0
      }
    }
    if (padding) {
      when (counter) {
        1 -> sb.append(byteToBase64(old)).append("==")
        2 -> sb.append(byteToBase64(old)).append("=")
      }
    }
    return sb.toString()
  }

  fun encode(data: ByteBuffer, padding: Boolean = true): String {
    val sb = StringBuilder()
    Base64EncodeOutput(appendable = sb, padding = padding).use {
      it.write(data = data)
    }
    return sb.toString()
  }

  fun decode(data: String, offset: Int = 0, length: Int = data.length - offset): ByteArray {
//        var buffer = ByteArray(Base64Decoder.calcSize(length))
//        var cursor = 0
//        val decoder = Base64Decoder {
//            buffer[cursor++] = it
//        }
//        for (i in offset until offset + length) {
//            decoder.add(data[i])
//        }
//        if (cursor != buffer.size) {
//            buffer = buffer.copyOf(cursor)
//        }
//        return buffer
//      -----------------------------
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
internal infix fun Byte.ushr(count: Int) = ((toInt() and 0xFF) ushr count).toByte()
internal infix fun Byte.shr(count: Int) = ((toInt() and 0xFF) shr count).toByte()
