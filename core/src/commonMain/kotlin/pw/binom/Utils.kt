package pw.binom

operator fun Long.get(index: Int): Byte {
    if (index !in 0 .. 7)
        throw IndexOutOfBoundsException()
    return ((this ushr (56 - 8 * index)) and 0xFF).toByte()
}

operator fun Int.get(index: Int): Byte {
    if (index !in 0 .. 3)
        throw IndexOutOfBoundsException()
    return ((this ushr (24 - 8 * index)) and 0xFF).toByte()
}

operator fun Short.get(index: Int): Byte {
    if (index !in 0 .. 1)
        throw IndexOutOfBoundsException()
    return ((this.toInt() ushr (8 - 8 * index)) and 0xFF).toByte()
}

fun Long.byteswap(): Long {
    val ch1 = (this ushr 56) and 0xFF
    val ch2 = (this ushr 48) and 0xFF
    val ch3 = (this ushr 40) and 0xFF
    val ch4 = (this ushr 32) and 0xFF
    val ch5 = (this ushr 24) and 0xFF
    val ch6 = (this ushr 16) and 0xFF
    val ch7 = (this ushr 8) and 0xFF
    val ch8 = (this ushr 0) and 0xFF

    return ((ch8 shl 56) or
            ((ch7 and 0xFF) shl 48) or
            ((ch6 and 0xFF) shl 40) or
            ((ch5 and 0xFF) shl 32) or
            ((ch4 and 0xFF) shl 24) or
            ((ch3 and 0xFF) shl 16) or
            ((ch2 and 0xFF) shl 8) or
            ((ch1 and 0xFF) shl 0))
}

fun Int.byteswap(): Int {
    val ch1 = (this ushr 24) and 0xFF
    val ch2 = (this ushr 16) and 0xFF
    val ch3 = (this ushr 8) and 0xFF
    val ch4 = (this ushr 0) and 0xFF
    return (ch4 shl 24) + (ch3 shl 16) + (ch2 shl 8) + (ch1 shl 0)
}

fun Short.byteswap(): Short {
    val ch1 = (this.toInt() ushr 8) and 0xFF
    val ch2 = (this.toInt() ushr 0) and 0xFF
    return ((ch2 shl 8) + (ch1 shl 0)).toShort()
}

fun Float.byteswap(): Float = Float.fromBits(toRawBits().byteswap())
fun Double.byteswap(): Double = Double.fromBits(toRawBits().byteswap())


fun Short.Companion.fromBytes(byte0: Byte, byte1: Byte) =
        (
                (byte0.toInt() shl 8) +
                        (byte1.toInt() shl 0)
                ).toShort()

fun Int.Companion.fromBytes(byte0: Byte, byte1: Byte, byte2: Byte, byte3: Byte) =
        (
                (byte0.toInt() shl 24) +
                        (byte1.toInt() shl 16) +
                        (byte2.toInt() shl 8) +
                        (byte3.toInt() shl 0)
                )

fun Long.Companion.fromBytes(byte0: Byte, byte1: Byte, byte2: Byte, byte3: Byte,
                             byte4: Byte, byte5: Byte, byte6: Byte, byte7: Byte) =
        (
                (byte0.toLong() shl 56) or
                        (byte1.toLong() shl 48) or
                        (byte2.toLong() shl 40) or
                        (byte3.toLong() shl 32) or
                        (byte4.toLong() shl 24) or
                        (byte5.toLong() shl 16) or
                        (byte6.toLong() shl 8) or
                        (byte7.toLong() shl 0)
                )