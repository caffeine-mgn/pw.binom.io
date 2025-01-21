package pw.binom

import pw.binom.io.ByteBuffer
import kotlin.jvm.JvmName

inline fun Long.eachByteIndexed(func: (Byte, Int) -> Unit) {
  func(ushr(56).toByte(), 0)
  func(ushr(48).toByte(), 1)
  func(ushr(40).toByte(), 2)
  func(ushr(32).toByte(), 3)
  func(ushr(24).toByte(), 4)
  func(ushr(16).toByte(), 5)
  func(ushr(8).toByte(), 6)
  func(ushr(0).toByte(), 7)
}

inline fun Long.eachByte(func: (Byte) -> Unit) {
  eachByteIndexed { value, _ -> func(value) }
}

@JvmName("Long_fromBytes1")
fun Long.Companion.fromBytes(
  byte0: Byte,
  byte1: Byte,
  byte2: Byte,
  byte3: Byte,
  byte4: Byte,
  byte5: Byte,
  byte6: Byte,
  byte7: Byte,
) =
  (byte0.toLong() and 0xFFL shl 56) +
    ((byte1).toLong() and 0xFFL shl 48) +
    ((byte2).toLong() and 0xFFL shl 40) +
    ((byte3).toLong() and 0xFFL shl 32) +
    ((byte4).toLong() and 0xFFL shl 24) +
    (byte5.toLong() and 0xFFL shl 16) +
    (byte6.toLong() and 0xFFL shl 8) +
    (byte7.toLong() and 0xFFL shl 0)

@JvmName("Long_fromBytes2")
inline fun Long.Companion.fromBytes(
  func: (Int) -> Byte,
) = fromBytes(
  func(0),
  func(1),
  func(2),
  func(3),
  func(4),
  func(5),
  func(6),
  func(7),
)

fun Long.toByteArray(): ByteArray {
  val result = ByteArray(Long.SIZE_BYTES)
  toByteArray(result)
  return result
}

fun Long.toByteArray(destination: ByteArray, offset: Int = 0): ByteArray {
  if (destination.size - offset < Long.SIZE_BYTES) {
    throw IllegalArgumentException("Not enough space for place Long")
  }
  eachByteIndexed { value, index ->
    destination[index + offset] = value
  }
  return destination
}

fun Long.Companion.fromBytes(source: ByteArray, offset: Int = 0): Long =
  fromBytes { index -> source[index + offset] }

fun Long.toByteBuffer(destination: ByteBuffer) {
  eachByte { destination.put(it) }
}

fun Long.toByteBuffer(): ByteArray {
  val result = ByteArray(Long.SIZE_BYTES)
  eachByteIndexed { value, index ->
    result[index] = value
  }
  return result
}

fun Long.Companion.fromBytes(source: ByteBuffer) = fromBytes { _ -> source.getByte() }
