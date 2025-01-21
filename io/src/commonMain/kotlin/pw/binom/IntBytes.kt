package pw.binom

import pw.binom.io.ByteBuffer
import kotlin.jvm.JvmName

inline fun Int.eachByte(func: (Byte) -> Unit) {
  eachByteIndexed { value, _ -> func(value) }
}

inline fun Int.eachByteIndexed(func: (Byte, Int) -> Unit) {
  func((this ushr (8 * (3 - 0))).toByte(), 0)
  func((this ushr (8 * (3 - 1))).toByte(), 1)
  func((this ushr (8 * (3 - 2))).toByte(), 2)
  func((this ushr (8 * (3 - 3))).toByte(), 3)
}

fun Int.toByteArray(destination: ByteArray, offset: Int = 0): ByteArray {
  if (destination.size - offset < Int.SIZE_BYTES) {
    throw IllegalArgumentException("Not enough space for place Int")
  }
  eachByteIndexed { value, index ->
    destination[index + offset] = value
  }
  return destination
}

fun Int.toByteArray(): ByteArray {
  val output = ByteArray(Int.SIZE_BYTES)
  eachByteIndexed { value, index ->
    output[index] = value
  }
  return output
}

/**
 * makes int from bytes using big-endian format
 */
@JvmName("Int_fromBytes1")
fun Int.Companion.fromBytes(byte0: Byte, byte1: Byte, byte2: Byte, byte3: Byte): Int =
  ((byte0.toInt() and 0xFF) shl 24) +
    ((byte1.toInt() and 0xFF) shl 16) +
    ((byte2.toInt() and 0xFF) shl 8) +
    ((byte3.toInt() and 0xFF) shl 0)

@JvmName("Int_fromBytes2")
inline fun Int.Companion.fromBytes(func: (Int) -> Byte): Int =
  fromBytes(
    func(0),
    func(1),
    func(2),
    func(3),
  )

@JvmName("Int_fromBytes3")
fun Int.Companion.fromBytes(source: ByteArray, offset: Int = 0) =
  fromBytes { index -> source[index + offset] }

/**
 * put int to [destination] using big-endian format
 */
fun Int.toByteBuffer(destination: ByteBuffer) {
  eachByte { destination.put(it) }
}

@JvmName("Int_fromBytes3")
fun Int.Companion.fromBytes(source: ByteBuffer) = fromBytes { source.getByte() }
