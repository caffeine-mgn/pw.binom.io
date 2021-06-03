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
value class Bitset32(val value: Int = 0) {
    operator fun get(index: Int): Boolean = value and (1 shl (31 - index)) != 0
    fun set(index: Int, value: Boolean) =
        Bitset32(
            if (value)
                (this.value or (1 shl (31 - index)))
            else
                (this.value.inv() or (1 shl 31 - index)).inv()
        )

    fun toInt() = value
    fun toUInt() = toInt().toUInt()

    /**
     * Returns byte from 4 bites using [index]
     *
     * @param index Offset for getting byte
     */
    fun getByte4(index: Int) = ((value ushr (32 - 4 - index)) and 0xF).toByte()

    /**
     * Sets 4 bits [value] to [Bitset32.value] using offset [index]. [value] must be less or equals 0xF
     *
     * Example:
     * ```
     * val set = Bitset32(0b100000) // 10 + 0000
     * set = set.setByte4(4, 0b1011)
     * println(set.toString(2)) // will print "101011". 10 + 1011
     * ```
     *
     * @param index offset for set data
     * @param value new value
     */
    fun setByte4(index: Int, value: Byte): Bitset32 {
        require(value <= 0xF)
        val leftPart = (this.value ushr (31 - index)) shl (31 - index)
        val rightPart = (this.value shl (index + 4)) ushr (index + 4)
        val valueInt = (value.toInt() and 0xF) shl (32 - 4 - index)
        return Bitset32(leftPart or valueInt or rightPart)
    }

    /**
     * Returns byte from 8 bites using [offset]
     *
     * Example:
     * ```
     * val set = Bitset(0b110101)// 1101 + 01
     * println(set.getByte(6)) //will print "1101". 6 offset = 4 bits of byte + 2 offset
     * ```
     *
     * @param offset Offset for getting byte
     */
    fun getByte8(offset: Int): Byte =
        ((value ushr (offset - 8)) and 0xFF).toByte()

    /**
     * Returns value as unsigned int in radix 2
     */
    override fun toString(): String = value.toBitsetString()

}

fun Int.toBitset32() = Bitset32(this)

/**
 * Returns int as bit set string. Example:
 * value = 0b00110100, result=00000000000000000000000000110100
 */
fun Int.toBitsetString(): String {
    val leftPart = toUInt().toString(2)
    var len = 32 - leftPart.length
    val sb = StringBuilder()
    while (len > 0) {
        len--
        sb.append("0")
    }
    sb.append(leftPart)
    return sb.toString()
}