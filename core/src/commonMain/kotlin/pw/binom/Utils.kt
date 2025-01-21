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

fun Short.reverse(): Short {
  val ch1 = (this.toInt() ushr 8) and 0xFF
  val ch2 = (this.toInt() ushr 0) and 0xFF
  return ((ch2 shl 8) + ch1).toShort()
}

// ----Int---- //

operator fun Int.get(index: Int): Byte {
  if (index !in 0 until Int.SIZE_BYTES) {
    throw IndexOutOfBoundsException("Can't get index $index. size: ${Int.SIZE_BYTES}")
  }
  return ((this ushr (8 * (3 - index)))).toByte()
}

fun Int.reverse(): Int {
  val ch1 = (this ushr 24) and 0xFF
  val ch2 = (this ushr 16) and 0xFF
  val ch3 = (this ushr 8) and 0xFF
  val ch4 = (this ushr 0) and 0xFF
  return (ch4 shl 24) + (ch3 shl 16) + (ch2 shl 8) + (ch1 shl 0)
}

// ----Int---- //

// ----Long---- //

operator fun Long.get(index: Int): Byte {
  if (index !in 0 until Long.SIZE_BYTES) {
    throw IndexOutOfBoundsException("Can't get index $index. size: ${Long.SIZE_BYTES}")
  }
  return ((this ushr (56 - 8 * index)) and 0xFF).toByte()
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

// ----Long---- //
