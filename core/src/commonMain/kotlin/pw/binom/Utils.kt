package pw.binom

import pw.binom.base64.shl

operator fun Long.get(index: Int): Byte {
    if (index !in 0..7)
        throw IndexOutOfBoundsException()
    return ((this ushr (56 - 8 * index)) and 0xFF).toByte()
}

operator fun Int.get(index: Int): Byte {
    if (index !in 0..3)
        throw IndexOutOfBoundsException()
    return ((this ushr (8 * (3 - index)))).toByte()
}

operator fun Short.get(index: Int): Byte {
    if (index !in 0..1)
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

fun Int.Companion.fromBytes(byte0: Byte, byte1: Byte, byte2: Byte, byte3: Byte): Int =
        ((byte0.toInt() and 0xFF) shl 24) +
                ((byte1.toInt() and 0xFF) shl 16) +
                ((byte2.toInt() and 0xFF) shl 8) +
                ((byte3.toInt() and 0xFF) shl 0)

fun Long.Companion.fromBytes(byte0: Byte, byte1: Byte, byte2: Byte, byte3: Byte,
                             byte4: Byte, byte5: Byte, byte6: Byte, byte7: Byte) =

        (byte0.toLong() and 0xFFL shl 56) +
                ((byte1).toLong() and 0xFFL shl 48) +
                ((byte2).toLong() and 0xFFL shl 40) +
                ((byte3).toLong() and 0xFFL shl 32) +
                ((byte4).toLong() and 0xFFL shl 24) +
                (byte5.toLong() and 0xFFL shl 16) +
                (byte6.toLong() and 0xFFL shl 8) +
                (byte7.toLong() and 0xFFL shl 0)

fun Long.toBytes(array: ByteArray, offset: Int = 0) {
    if (array.size - offset < Long.SIZE_BYTES)
        throw IllegalArgumentException()
    array[0] = ushr(56).toByte()
    array[1] = ushr(48).toByte()
    array[2] = ushr(40).toByte()
    array[3] = ushr(32).toByte()
    array[4] = ushr(24).toByte()
    array[5] = ushr(16).toByte()
    array[6] = ushr(8).toByte()
    array[7] = ushr(0).toByte()
}

fun Long.Companion.fromBytes(readBuffer: ByteArray) = (readBuffer[0] shl 56) +
        ((readBuffer[1].toLong() and 0xFFL) shl 48) +
        ((readBuffer[2].toLong() and 0xFFL) shl 40) +
        ((readBuffer[3].toLong() and 0xFFL) shl 32) +
        ((readBuffer[4].toLong() and 0xFFL) shl 24) +
        (readBuffer[5].toLong() and 0xFFL shl 16) +
        (readBuffer[6].toLong() and 0xFFL shl 8) +
        (readBuffer[7].toLong() and 0xFFL shl 0)