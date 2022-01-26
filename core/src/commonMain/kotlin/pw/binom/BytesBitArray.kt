package pw.binom

import pw.binom.base64.shl
import pw.binom.base64.ushr
import kotlin.experimental.and
import kotlin.jvm.JvmInline

@JvmInline
value class BytesBitArray(val data: ByteArray) : BitArray {

    override operator fun get(index: Int): Boolean {
        val value = data[index / Byte.SIZE_BITS]
        val mask = 1.toByte() shl (Byte.SIZE_BITS - 1 - (index % Byte.SIZE_BITS))
        return value and mask != 0.toByte()
    }

    override fun update(index: Int, value: Boolean): BytesBitArray {
        val result = BytesBitArray(data.copyOf())
        result[index] = value
        return result
    }

    override fun getByte4(index: Int): Byte {
        val byteIndex = index / Byte.SIZE_BITS
        val value = data[byteIndex]
        if (index % Byte.SIZE_BITS == 0) {
            return value ushr 4
        }
        if (index % 4 == 0) {
            return value and 0xF
        }
        val mod = index % Byte.SIZE_BITS
        if (mod <= 4) {
            return value ushr Byte.SIZE_BITS - 4 - mod
        }
        val value2 = data[byteIndex + 1]
        val v1 = value shl (4 - (Byte.SIZE_BITS - mod))
        val v2 = value2 ushr (Byte.SIZE_BITS - ((mod + 4) % Byte.SIZE_BITS))
        return (v1 + v2).toByte()
    }

    fun setByte4(index: Int, value: Byte) {
        val byteIndex = index / Byte.SIZE_BITS
        val oldValue = data[byteIndex]
        val bitOffset = index % Byte.SIZE_BITS
        if (bitOffset == 0) {
            data[byteIndex] = ((oldValue ushr 4) + (value shl 4)).toByte()
            return
        }
        if (index % 4 == 0) {
            data[byteIndex] = (((oldValue ushr 4) shl 4) + value and 0xF).toByte()
            return
        }

        if (bitOffset < 4) {
            val l = 4 + (4 - bitOffset)
            val r = 4 + bitOffset
            val insertValue = value shl (4 - bitOffset)
            val left = (oldValue ushr l) shl l
            val right = (oldValue shl r) ushr r
            val result = (left + insertValue + right).toByte()
            data[byteIndex] = result
            return
        }

        val left = (((oldValue ushr bitOffset) shl bitOffset) + ((value and 0xF) ushr (4 - bitOffset))).toByte()
        data[byteIndex] = left
        val oldValue2 = data[byteIndex + 1]

        val valueRight = value shl (4 + bitOffset)
        val right = (((oldValue2 shl bitOffset) ushr bitOffset) + valueRight).toByte()
        data[byteIndex + 1] = right
    }

    override fun updateByte4(index: Int, value: Byte): BytesBitArray {
        val ret = BytesBitArray(data.copyOf())
        ret.setByte4(index = index, value = value)
        return ret
    }

    override fun getByte8(index: Int): Byte {
        val mod = index % Byte.SIZE_BITS
        val byteIndex = index / Byte.SIZE_BITS
        if (mod == 0) {
            return data[byteIndex]
        }
        val value1 = data[byteIndex]
        val value2 = data[byteIndex + 1]
        val leftPart = (value1 shl mod)
        val rightPart = value2 ushr (Byte.SIZE_BITS - mod)
        return (leftPart + rightPart).toByte()
    }

    operator fun set(index: Int, value: Boolean) {
        val value1 = data[index / Byte.SIZE_BITS].toInt() and 0xFF
        val t = 1 shl (Byte.SIZE_BITS - 1 - index % Byte.SIZE_BITS)
        data[index / Byte.SIZE_BITS] = (
            if (value) {
                (value1 or t)
            } else {
                (value1.inv() or t).inv()
            }
            ).toByte()
    }

    override val size
        get() = data.size * Byte.SIZE_BITS

    override fun toString(): String {
        val sb = StringBuilder(size)
        data.forEach { byte ->
            byte.toBitsetString(sb)
        }
        return sb.toString()
    }
}

private fun Byte.toBitsetString(sb2: StringBuilder = StringBuilder()): String {
    repeat(Byte.SIZE_BITS) { index ->
        val mask = 0b1.toByte() shl (Byte.SIZE_BITS - 1 - index)
        sb2.append(if (this and mask != 0.toByte()) "1" else "0")
    }
    return sb2.toString()
}
