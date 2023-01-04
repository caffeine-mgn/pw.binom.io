package pw.binom

import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

fun Short.toInt8Array(dest: Int8Array, offset: Int = 0): Int8Array {
    if (dest.length - offset < Short.SIZE_BYTES) {
        throw IllegalArgumentException("Not enough space for place Short")
    }
    dest[0 + offset] = ((this.toInt() ushr (8 - 8 * 0)) and 0xFF).toByte()
    dest[1 + offset] = ((this.toInt() ushr (8 - 8 * 1)) and 0xFF).toByte()
    return dest
}

fun Short.toInt8Array(): Int8Array {
    val out = Int8Array(Short.SIZE_BYTES)
    toInt8Array(out)
    return out
}

fun Int.toInt8Array(dest: Int8Array, offset: Int = 0): Int8Array {
    if (dest.length - offset < Int.SIZE_BYTES) {
        throw IllegalArgumentException("Not enough space for place Int")
    }
    dest[0 + offset] = (this ushr (8 * (3 - 0))).toByte()
    dest[1 + offset] = (this ushr (8 * (3 - 1))).toByte()
    dest[2 + offset] = (this ushr (8 * (3 - 2))).toByte()
    dest[3 + offset] = (this ushr (8 * (3 - 3))).toByte()
    return dest
}

fun Int.toInt8Array(): Int8Array {
    val out = Int8Array(Int.SIZE_BYTES)
    toInt8Array(out)
    return out
}

fun Long.toInt8Array(dest: Int8Array, offset: Int = 0): Int8Array {
    if (dest.length - offset < Long.SIZE_BYTES) {
        throw IllegalArgumentException()
    }
    dest[0 + offset] = ushr(56).toByte()
    dest[1 + offset] = ushr(48).toByte()
    dest[2 + offset] = ushr(40).toByte()
    dest[3 + offset] = ushr(32).toByte()
    dest[4 + offset] = ushr(24).toByte()
    dest[5 + offset] = ushr(16).toByte()
    dest[6 + offset] = ushr(8).toByte()
    dest[7 + offset] = ushr(0).toByte()
    return dest
}

fun Long.toInt8Array(): Int8Array {
    val out = Int8Array(Long.SIZE_BYTES)
    toInt8Array(out)
    return out
}

fun Float.toInt8Array(dest: Int8Array, offset: Int = 0): Int8Array =
    this.toRawBits().toInt8Array(dest = dest, offset = offset)

fun Float.toInt8Array(): Int8Array {
    val out = Int8Array(Float.SIZE_BYTES)
    toInt8Array(out)
    return out
}

fun Double.toInt8Array(dest: Int8Array, offset: Int = 0): Int8Array =
    this.toRawBits().toInt8Array(dest = dest, offset = offset)

fun Double.toInt8Array(): Int8Array {
    val out = Int8Array(Double.SIZE_BYTES)
    toInt8Array(out)
    return out
}

fun Short.Companion.fromBytes(readBuffer: Int8Array, offset: Int = 0): Short =
    fromBytes(
        readBuffer[0 + offset],
        readBuffer[1 + offset],
    )

fun Int.Companion.fromBytes(readBuffer: Int8Array, offset: Int = 0): Int =
    fromBytes(
        readBuffer[0 + offset],
        readBuffer[1 + offset],
        readBuffer[2 + offset],
        readBuffer[3 + offset],
    )

fun Long.Companion.fromBytes(readBuffer: Int8Array, offset: Int = 0): Long =
    fromBytes(
        readBuffer[0 + offset],
        readBuffer[1 + offset],
        readBuffer[2 + offset],
        readBuffer[3 + offset],
        readBuffer[4 + offset],
        readBuffer[5 + offset],
        readBuffer[6 + offset],
        readBuffer[7 + offset],
    )
