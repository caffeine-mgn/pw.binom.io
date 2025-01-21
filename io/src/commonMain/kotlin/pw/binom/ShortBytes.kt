package pw.binom

import pw.binom.io.ByteBuffer
import kotlin.jvm.JvmName

inline fun Short.eachByteIndexed(func: (Byte, Int) -> Unit) {
  func(((this.toInt() ushr (8 - 8 * 0)) and 0xFF).toByte(), 0)
  func(((this.toInt() ushr (8 - 8 * 1)) and 0xFF).toByte(), 1)
}

inline fun Short.eachByte(func: (Byte) -> Unit) {
  eachByteIndexed { value, index ->
    func(value)
  }
}

fun Short.toByteArray(): ByteArray {
  val output = ByteArray(Short.SIZE_BYTES)
  eachByteIndexed { value, index ->
    output[index] = value
  }
  return output
}

@JvmName("Short_fromBytes1")
fun Short.Companion.fromBytes(byte0: Byte, byte1: Byte) =
  ((byte0.toInt() and 0xFF shl 8) + (byte1.toInt() and 0xFF)).toShort()

@JvmName("Short_fromBytes2")
inline fun Short.Companion.fromBytes(func: (Int) -> Byte) =
  fromBytes(func(0), func(1))

@JvmName("Short_fromBytes3")
fun Short.Companion.fromBytes(source: ByteBuffer): Short =
  fromBytes(source.getByte(), source.getByte())

@JvmName("Short_fromBytes3")
fun Short.Companion.fromBytes(source: ByteArray, offset: Int = 0) =
  fromBytes(
    source[0 + offset],
    source[1 + offset],
  )

fun Short.toByteArray(destination: ByteArray, offset: Int = 0): ByteArray {
  if (destination.size - offset < Short.SIZE_BYTES) {
    throw IllegalArgumentException("Not enough space for place Short")
  }
  eachByteIndexed { value, index ->
    destination[index + offset] = value
  }
  return destination
}

fun Short.toByteBuffer(destination: ByteBuffer) {
  destination.put(((this.toInt() ushr (8 - 8 * 0)) and 0xFF).toByte())
  destination.put(((this.toInt() ushr (8 - 8 * 1)) and 0xFF).toByte())
}
