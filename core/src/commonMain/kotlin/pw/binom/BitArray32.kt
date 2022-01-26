package pw.binom

import kotlin.jvm.JvmInline

/**
 * Implements bitset based on [Int]. Index 0 is the most left bit. For example: index 30 in value 0b010 is 1, index 31 and 29 is 0
 *
 * Example:
 * ```
 * var data = Bitset32()
 * assertFalse(data[0])
 * assertFalse(data[1])
 * data = data.set(0,true)
 * assertTrue(data[0])
 * assertFalse(data[1])
 * ```
 */
@JvmInline
value class BitArray32(val value: Int = 0) : BitArray {
    override val size: Int
        get() = 32

    override operator fun get(index: Int): Boolean = value and (1 shl (MAX_BITS_1 - index)) != 0
    override fun update(index: Int, value: Boolean) =
        BitArray32(
            if (value)
                (this.value or (1 shl (MAX_BITS_1 - index)))
            else
                (this.value.inv() or (1 shl MAX_BITS_1 - index)).inv()
        )

    fun toInt() = value
    fun toUInt() = toInt().toUInt()
    override fun getByte4(index: Int) = ((value ushr (MAX_BITS - 4 - index)) and 0xF).toByte()
    override fun updateByte4(index: Int, value: Byte): BitArray32 {
        require(value <= 0xF)
        val leftPart = (this.value ushr (MAX_BITS_1 - index)) shl (MAX_BITS_1 - index)
        val rightPart = (this.value shl (index + 4)) ushr (index + 4)
        val valueInt = (value.toInt() and 0xF) shl (MAX_BITS - 4 - index)
        return BitArray32(leftPart or valueInt or rightPart)
    }

    override fun getByte8(index: Int): Byte =
        ((value ushr (size - 8 - index)) and 0xFF).toByte()

    /**
     * Returns value as unsigned int in radix 2
     */
    override fun toString(): String = value.toBitsetString()

}

fun Int.toBitset32() = BitArray32(this)

/**
 * Returns int as bit set string. Example:
 * value = 0b00110100, result=00000000000000000000000000110100
 */
fun Int.toBitsetString(): String {
    val leftPart = toUInt().toString(2)
    var len = MAX_BITS - leftPart.length
    val sb = StringBuilder()
    while (len > 0) {
        len--
        sb.append("0")
    }
    sb.append(leftPart)
    return sb.toString()
}

private const val MAX_BITS = Int.SIZE_BITS
private const val MAX_BITS_1 = MAX_BITS - 1
