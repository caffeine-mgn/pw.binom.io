package pw.binom

import org.khronos.webgl.Int8Array
import org.khronos.webgl.set

fun Long.toInt8Array(array: Int8Array, offset: Int = 0) {
    if (array.length - offset < Long.SIZE_BYTES) {
        throw IllegalArgumentException()
    }
    array[0 + offset] = ushr(56).toByte()
    array[1 + offset] = ushr(48).toByte()
    array[2 + offset] = ushr(40).toByte()
    array[3 + offset] = ushr(32).toByte()
    array[4 + offset] = ushr(24).toByte()
    array[5 + offset] = ushr(16).toByte()
    array[6 + offset] = ushr(8).toByte()
    array[7 + offset] = ushr(0).toByte()
}

fun Long.toInt8Array(): Int8Array {
    val out = Int8Array(Long.SIZE_BYTES)
    toInt8Array(out)
    return out
}
