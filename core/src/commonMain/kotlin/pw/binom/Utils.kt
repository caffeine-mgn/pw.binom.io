@file:JvmName("BinomInternalUtils")
package pw.binom

import pw.binom.io.ByteBuffer
import kotlin.experimental.inv
import kotlin.jvm.JvmName

operator fun Long.get(index: Int): Byte {
  if (index !in 0 until Long.SIZE_BYTES) {
    throw IndexOutOfBoundsException("Can't get index $index. size: ${Long.SIZE_BYTES}")
  }
  return ((this ushr (56 - 8 * index)) and 0xFF).toByte()
}

operator fun Int.get(index: Int): Byte {
  if (index !in 0 until Int.SIZE_BYTES) {
    throw IndexOutOfBoundsException("Can't get index $index. size: ${Int.SIZE_BYTES}")
  }
  return ((this ushr (8 * (3 - index)))).toByte()
}

operator fun Short.get(index: Int): Byte {
  if (index !in 0 until Short.SIZE_BYTES) {
    throw IndexOutOfBoundsException("Can't get index $index. size: ${Short.SIZE_BYTES}")
  }
  return ((this.toInt() ushr (8 - 8 * index)) and 0xFF).toByte()
}

fun Long.toByteBuffer(destination: ByteBuffer) {
//  destination.write(toByteArray())
  destination.put(((this ushr (56 - 8 * 0)) and 0xFF).toByte())
  destination.put(((this ushr (56 - 8 * 1)) and 0xFF).toByte())
  destination.put(((this ushr (56 - 8 * 2)) and 0xFF).toByte())
  destination.put(((this ushr (56 - 8 * 3)) and 0xFF).toByte())
  destination.put(((this ushr (56 - 8 * 4)) and 0xFF).toByte())
  destination.put(((this ushr (56 - 8 * 5)) and 0xFF).toByte())
  destination.put(((this ushr (56 - 8 * 6)) and 0xFF).toByte())
  destination.put(((this ushr (56 - 8 * 7)) and 0xFF).toByte())
}

fun Long.toByteBuffer(): ByteArray {
  val result = ByteArray(Long.SIZE_BYTES)
  toByteArray(destination = result)
  return result
}

//fun Long.toByteBuffer(destination: ByteArray, destOffset: Int = 0): ByteArray {
//  require(destination.size - destOffset >= Long.SIZE_BYTES) { "Not available space for storage long" }
//  destination[0 + destOffset] = ((this ushr (56 - 8 * 0)) and 0xFF).toByte()
//  destination[1 + destOffset] = ((this ushr (56 - 8 * 1)) and 0xFF).toByte()
//  destination[2 + destOffset] = ((this ushr (56 - 8 * 2)) and 0xFF).toByte()
//  destination[3 + destOffset] = ((this ushr (56 - 8 * 3)) and 0xFF).toByte()
//  destination[4 + destOffset] = ((this ushr (56 - 8 * 4)) and 0xFF).toByte()
//  destination[5 + destOffset] = ((this ushr (56 - 8 * 5)) and 0xFF).toByte()
//  destination[6 + destOffset] = ((this ushr (56 - 8 * 6)) and 0xFF).toByte()
//  destination[7 + destOffset] = ((this ushr (56 - 8 * 7)) and 0xFF).toByte()
//  return destination
//}

fun Float.toByteBuffer(destination: ByteBuffer) {
  toRawBits().toByteBuffer(destination)
}

fun Float.toByteArray(destination: ByteArray, offset: Int = 0): ByteArray {
  if (destination.size - offset < Float.SIZE_BYTES) {
    throw IllegalArgumentException("Not enough space for place Float")
  }
  return toRawBits().toByteArray(destination = destination, offset = offset)
}

fun Float.toByteArray(): ByteArray {
  val output = ByteArray(Float.SIZE_BYTES)
  toByteArray(output)
  return output
}

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
  val output = ByteArray(Double.SIZE_BYTES)
  toByteArray(output)
  return output
}

/**
 * put int to [destination] using big-endian format
 */
fun Int.toByteBuffer(destination: ByteBuffer) {
  destination.put((this ushr (8 * (3 - 0))).toByte())
  destination.put((this ushr (8 * (3 - 1))).toByte())
  destination.put((this ushr (8 * (3 - 2))).toByte())
  destination.put((this ushr (8 * (3 - 3))).toByte())
}

fun Int.toByteArray(destination: ByteArray, offset: Int = 0): ByteArray {
  if (destination.size - offset < Int.SIZE_BYTES) {
    throw IllegalArgumentException("Not enough space for place Int")
  }
  destination[0 + offset] = (this ushr (8 * (3 - 0))).toByte()
  destination[1 + offset] = (this ushr (8 * (3 - 1))).toByte()
  destination[2 + offset] = (this ushr (8 * (3 - 2))).toByte()
  destination[3 + offset] = (this ushr (8 * (3 - 3))).toByte()
  return destination
}

fun Int.toByteArray(): ByteArray {
  val output = ByteArray(Int.SIZE_BYTES)
  toByteArray(output)
  return output
}

fun Short.toByteBuffer(destination: ByteBuffer) {
  destination.put(((this.toInt() ushr (8 - 8 * 0)) and 0xFF).toByte())
  destination.put(((this.toInt() ushr (8 - 8 * 1)) and 0xFF).toByte())
}

fun Short.toByteArray(destination: ByteArray, offset: Int = 0): ByteArray {
  if (destination.size - offset < Short.SIZE_BYTES) {
    throw IllegalArgumentException("Not enough space for place Short")
  }
  destination[0 + offset] = ((this.toInt() ushr (8 - 8 * 0)) and 0xFF).toByte()
  destination[1 + offset] = ((this.toInt() ushr (8 - 8 * 1)) and 0xFF).toByte()
  return destination
}

fun Short.toByteArray(): ByteArray {
  val output = ByteArray(Short.SIZE_BYTES)
  toByteArray(output)
  return output
}

// fun Short.dump(): ByteArray {
//    val output = ByteArray(Short.SIZE_BYTES)
//    output[0] = ((this.toInt() ushr (8 - 8 * 0)) and 0xFF).toByte()
//    output[1] = ((this.toInt() ushr (8 - 8 * 1)) and 0xFF).toByte()
//    return output
// }

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

fun Int.reverse(): Int {
  val ch1 = (this ushr 24) and 0xFF
  val ch2 = (this ushr 16) and 0xFF
  val ch3 = (this ushr 8) and 0xFF
  val ch4 = (this ushr 0) and 0xFF
  return (ch4 shl 24) + (ch3 shl 16) + (ch2 shl 8) + (ch1 shl 0)
}

fun Short.reverse(): Short {
  val ch1 = (this.toInt() ushr 8) and 0xFF
  val ch2 = (this.toInt() ushr 0) and 0xFF
  return ((ch2 shl 8) + ch1).toShort()
}

fun Float.reverse(): Float = Float.fromBits(toRawBits().reverse())
fun Double.reverse(): Double = Double.fromBits(toRawBits().reverse())

fun Short.Companion.fromBytes(byte0: Byte, byte1: Byte) =
  ((byte0.toInt() and 0xFF shl 8) + (byte1.toInt() and 0xFF)).toShort()

fun Short.Companion.fromBytes(source: ByteBuffer): Short {
  val b0 = source.getByte()
  val b1 = source.getByte()
  return fromBytes(b0, b1)
}

fun Short.Companion.fromBytes(source: ByteArray, offset: Int = 0) =
  fromBytes(
    source[0 + offset],
    source[1 + offset],
  )

@JvmName("Int_fromBytes3")
fun Int.Companion.fromBytes(source: ByteBuffer): Int {
  val b0 = source.getByte()
  val b1 = source.getByte()
  val b2 = source.getByte()
  val b3 = source.getByte()
  return fromBytes(b0, b1, b2, b3)
}

fun Long.Companion.fromBytes(source: ByteBuffer): Long {
  val b0 = source.getByte()
  val b1 = source.getByte()
  val b2 = source.getByte()
  val b3 = source.getByte()
  val b4 = source.getByte()
  val b5 = source.getByte()
  val b6 = source.getByte()
  val b7 = source.getByte()
  return fromBytes(b0, b1, b2, b3, b4, b5, b6, b7)
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
fun Int.Companion.fromBytes(source: ByteArray, offset: Int = 0) =
  ((source[0 + offset].toInt() and 0xFF) shl 24) +
    ((source[1 + offset].toInt() and 0xFF) shl 16) +
    ((source[2 + offset].toInt() and 0xFF) shl 8) +
    ((source[3 + offset].toInt() and 0xFF) shl 0)

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

fun Long.toByteArray(): ByteArray {
  val result = ByteArray(Long.SIZE_BYTES)
  toByteArray(result)
  return result
}

//infix fun Long.or(other: Long) = Long.fromBytes(
//  (this[0].toInt() or other[0].toInt()).toByte(),
//  (this[1].toInt() or other[1].toInt()).toByte(),
//  (this[2].toInt() or other[2].toInt()).toByte(),
//  (this[3].toInt() or other[3].toInt()).toByte(),
//  (this[4].toInt() or other[4].toInt()).toByte(),
//  (this[5].toInt() or other[5].toInt()).toByte(),
//  (this[6].toInt() or other[6].toInt()).toByte(),
//  (this[7].toInt() or other[7].toInt()).toByte(),
//)

//infix fun Long.and(other: Long) = Long.fromBytes(
//  (this[0].toInt() and other[0].toInt()).toByte(),
//  (this[1].toInt() and other[1].toInt()).toByte(),
//  (this[2].toInt() and other[2].toInt()).toByte(),
//  (this[3].toInt() and other[3].toInt()).toByte(),
//  (this[4].toInt() and other[4].toInt()).toByte(),
//  (this[5].toInt() and other[5].toInt()).toByte(),
//  (this[6].toInt() and other[6].toInt()).toByte(),
//  (this[7].toInt() and other[7].toInt()).toByte(),
//)

//infix fun Long.xor(other: Long) = Long.fromBytes(
//  (this[0].toInt() xor other[0].toInt()).toByte(),
//  (this[1].toInt() xor other[1].toInt()).toByte(),
//  (this[2].toInt() xor other[2].toInt()).toByte(),
//  (this[3].toInt() xor other[3].toInt()).toByte(),
//  (this[4].toInt() xor other[4].toInt()).toByte(),
//  (this[5].toInt() xor other[5].toInt()).toByte(),
//  (this[6].toInt() xor other[6].toInt()).toByte(),
//  (this[7].toInt() xor other[7].toInt()).toByte(),
//)

//fun Long.inv() = Long.fromBytes(
//  this[0].inv(),
//  this[1].inv(),
//  this[2].inv(),
//  this[3].inv(),
//  this[4].inv(),
//  this[5].inv(),
//  this[6].inv(),
//  this[7].inv(),
//)

fun Long.toByteArray(destination: ByteArray, offset: Int = 0): ByteArray {
  if (destination.size - offset < Long.SIZE_BYTES) {
    throw IllegalArgumentException("Not enough space for place Long")
  }
  destination[0 + offset] = ushr(56).toByte()
  destination[1 + offset] = ushr(48).toByte()
  destination[2 + offset] = ushr(40).toByte()
  destination[3 + offset] = ushr(32).toByte()
  destination[4 + offset] = ushr(24).toByte()
  destination[5 + offset] = ushr(16).toByte()
  destination[6 + offset] = ushr(8).toByte()
  destination[7 + offset] = ushr(0).toByte()
  return destination
}

fun Long.Companion.fromBytes(source: ByteArray, offset: Int = 0): Long =
  fromBytes(
    source[0 + offset],
    source[1 + offset],
    source[2 + offset],
    source[3 + offset],
    source[4 + offset],
    source[5 + offset],
    source[6 + offset],
    source[7 + offset],
  )
