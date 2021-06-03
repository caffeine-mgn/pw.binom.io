package pw.binom

import kotlin.jvm.JvmInline

@JvmInline
value class BytesBitArray(val data: ByteArray) {
    operator fun get(index: Int): Boolean {
        val value = data[index / Byte.SIZE_BITS].toInt() and 0xFF
        return (value ushr (index % Byte.SIZE_BITS)) == 1
    }

    operator fun set(index: Int, value: Boolean) {
        val value1 = data[index / Byte.SIZE_BITS].toInt() and 0xFF
        val t = 1 shl (index % Byte.SIZE_BITS)

        data[index / Byte.SIZE_BITS] = (if (value) {
            (value1 or t)
        } else {
            (value1.inv() or t).inv()
        }).toByte()
    }

    val size
        get() = data.size * Byte.SIZE_BITS
}