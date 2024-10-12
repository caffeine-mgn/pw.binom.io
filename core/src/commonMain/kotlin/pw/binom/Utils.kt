@file:JvmName("BinomInternalUtils")

package pw.binom

import pw.binom.io.ByteBuffer
import kotlin.jvm.JvmName

// ----Float---- //
fun Float.toByteBuffer(destination: ByteBuffer) {
  toRawBits().toByteBuffer(destination)
}

fun Float.toByteArray(destination: ByteArray, offset: Int = 0): ByteArray {
  if (destination.size - offset < Float.SIZE_BYTES) {
    throw IllegalArgumentException("Not enough space for place Float")
  }
  return toRawBits().toByteArray(destination = destination, offset = offset)
}

fun Float.toByteArray(): ByteArray = toRawBits().toByteArray()

fun Float.reverse(): Float = Float.fromBits(toRawBits().reverse())


// ----Double---- //

fun Double.toByteBuffer(destination: ByteBuffer) {
  toRawBits().toByteBuffer(destination)
}

fun Double.toByteArray(destination: ByteArray, offset: Int = 0): ByteArray {
  if (destination.size - offset < Double.SIZE_BYTES) {
    throw IllegalArgumentException("Not enough space for place Double")
  }
  return toRawBits().toByteArray(destination = destination, offset = offset)
}

fun Double.toByteArray(): ByteArray {
  return toRawBits().toByteArray()
}

fun Double.reverse(): Double = Double.fromBits(toRawBits().reverse())

// ----Short---- //

operator fun Short.get(index: Int): Byte {
  if (index !in 0 until Short.SIZE_BYTES) {
    throw IndexOutOfBoundsException("Can't get index $index. size: ${Short.SIZE_BYTES}")
  }
  return ((this.toInt() ushr (8 - 8 * index)) and 0xFF).toByte()
}

fun Short.toByteBuffer(destination: ByteBuffer) {
  destination.put(((this.toInt() ushr (8 - 8 * 0)) and 0xFF).toByte())
  destination.put(((this.toInt() ushr (8 - 8 * 1)) and 0xFF).toByte())
}

fun Short.toByteArray(destination: ByteArray, offset: Int = 0): ByteArray {
  if (destination.size - offset < Short.SIZE_BYTES) {
    throw IllegalArgumentException("Not enough space for place Short")
  }
  eachByteIndexed { value, index ->
    destination[index + offset] = value
  }
  return destination
}

inline fun Short.eachByte(func: (Byte) -> Unit) {
  eachByteIndexed { value, index ->
    func(value)
  }
}

inline fun Short.eachByteIndexed(func: (Byte, Int) -> Unit) {
  func(((this.toInt() ushr (8 - 8 * 0)) and 0xFF).toByte(), 0)
  func(((this.toInt() ushr (8 - 8 * 1)) and 0xFF).toByte(), 1)
}

fun Short.toByteArray(): ByteArray {
  val output = ByteArray(Short.SIZE_BYTES)
  eachByteIndexed { value, index ->
    output[index] = value
  }
  return output
}

fun Short.reverse(): Short {
  val ch1 = (this.toInt() ushr 8) and 0xFF
  val ch2 = (this.toInt() ushr 0) and 0xFF
  return ((ch2 shl 8) + ch1).toShort()
}

@JvmName("Short_fromBytes1")
fun Short.Companion.fromBytes(byte0: Byte, byte1: Byte) =
  ((byte0.toInt() and 0xFF shl 8) + (byte1.toInt() and 0xFF)).toShort()

@JvmName("Short_fromBytes2")
inline fun Short.Companion.fromBytes(func: (Int) -> Byte) =
  ((func(0).toInt() and 0xFF shl 8) + (func(1).toInt() and 0xFF)).toShort()

@JvmName("Short_fromBytes3")
fun Short.Companion.fromBytes(source: ByteBuffer): Short {
  val b0 = source.getByte()
  val b1 = source.getByte()
  return fromBytes(b0, b1)
}

@JvmName("Short_fromBytes3")
fun Short.Companion.fromBytes(source: ByteArray, offset: Int = 0) =
  fromBytes(
    source[0 + offset],
    source[1 + offset],
  )
// ----Int---- //

operator fun Int.get(index: Int): Byte {
  if (index !in 0 until Int.SIZE_BYTES) {
    throw IndexOutOfBoundsException("Can't get index $index. size: ${Int.SIZE_BYTES}")
  }
  return ((this ushr (8 * (3 - index)))).toByte()
}

/**
 * put int to [destination] using big-endian format
 */
fun Int.toByteBuffer(destination: ByteBuffer) {
  eachByte { destination.put(it) }
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

inline fun Int.eachByte(func: (Byte) -> Unit) {
  eachByteIndexed { value, _ -> func(value) }
}

inline fun Int.eachByteIndexed(func: (Byte, Int) -> Unit) {
  func((this ushr (8 * (3 - 0))).toByte(), 0)
  func((this ushr (8 * (3 - 1))).toByte(), 1)
  func((this ushr (8 * (3 - 2))).toByte(), 2)
  func((this ushr (8 * (3 - 3))).toByte(), 3)
}

fun Int.toByteArray(): ByteArray {
  val output = ByteArray(Int.SIZE_BYTES)
  eachByteIndexed { value, index ->
    output[index] = value
  }
  return output
}

fun Int.reverse(): Int {
  val ch1 = (this ushr 24) and 0xFF
  val ch2 = (this ushr 16) and 0xFF
  val ch3 = (this ushr 8) and 0xFF
  val ch4 = (this ushr 0) and 0xFF
  return (ch4 shl 24) + (ch3 shl 16) + (ch2 shl 8) + (ch1 shl 0)
}

@JvmName("Int_fromBytes3")
fun Int.Companion.fromBytes(source: ByteBuffer) = fromBytes { source.getByte() }

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
  ((func(0).toInt() and 0xFF) shl 24) +
    ((func(1).toInt() and 0xFF) shl 16) +
    ((func(2).toInt() and 0xFF) shl 8) +
    ((func(3).toInt() and 0xFF) shl 0)

@JvmName("Int_fromBytes3")
fun Int.Companion.fromBytes(source: ByteArray, offset: Int = 0) =
  fromBytes { index -> source[index + offset] }
// ----Int---- //

// ----Long---- //

operator fun Long.get(index: Int): Byte {
  if (index !in 0 until Long.SIZE_BYTES) {
    throw IndexOutOfBoundsException("Can't get index $index. size: ${Long.SIZE_BYTES}")
  }
  return ((this ushr (56 - 8 * index)) and 0xFF).toByte()
}


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

fun Long.reverse(): Long {
  val ch1 = (this ushr 56) and 0xFF
  val ch2 = (this ushr 48) and 0xFF
  val ch3 = (this ushr 40) and 0xFF
  val ch4 = (this ushr 32) and 0xFF
  val ch5 = (this ushr 24) and 0xFF
  val ch6 = (this ushr 16) and 0xFF
  val ch7 = (this ushr 8) and 0xFF
  val ch8 = (this ushr 0) and 0xFF

  return (
    (ch8 shl 56) or
      ((ch7 and 0xFF) shl 48) or
      ((ch6 and 0xFF) shl 40) or
      ((ch5 and 0xFF) shl 32) or
      ((ch4 and 0xFF) shl 24) or
      ((ch3 and 0xFF) shl 16) or
      ((ch2 and 0xFF) shl 8) or
      ((ch1 and 0xFF) shl 0)
    )
}

fun Long.Companion.fromBytes(source: ByteBuffer) = fromBytes { _ -> source.getByte() }

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
) =
  (func(0).toLong() and 0xFFL shl 56) +
    (func(1).toLong() and 0xFFL shl 48) +
    (func(2).toLong() and 0xFFL shl 40) +
    (func(3).toLong() and 0xFFL shl 32) +
    (func(4).toLong() and 0xFFL shl 24) +
    (func(5).toLong() and 0xFFL shl 16) +
    (func(6).toLong() and 0xFFL shl 8) +
    (func(7).toLong() and 0xFFL shl 0)

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

inline fun Long.eachByte(func: (Byte) -> Unit) {
  eachByteIndexed { value, _ -> func(value) }
}

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

fun Long.Companion.fromBytes(source: ByteArray, offset: Int = 0): Long =
  fromBytes { index -> source[index + offset] }
// ----Long---- //
