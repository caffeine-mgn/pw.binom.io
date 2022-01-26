package pw.binom

sealed interface BitArray {
    val size: Int
    val lastIndex: Int
        get() = size - 1

    operator fun get(index: Int): Boolean
    fun update(index: Int, value: Boolean): BitArray
    fun toByteArray() = BooleanArray(size) { this[it] }
    fun full(value: Boolean, startIndex: Int = 0, endIndex: Int = lastIndex): BitArray {
        var e = this
        for (i in startIndex..lastIndex) {
            e = e.update(i, value)
        }
        return e
    }

    /**
     * Returns byte from 4 bites using [index]
     *
     * @param index Offset for getting byte
     */
    fun getByte4(index: Int): Byte

    /**
     * Sets 4 bits [value] to this inline value using offset [index]. [value] must be less or equals 0xF
     *
     * Example:
     * ```
     * val set = Bitset64(0b100000) // 10 + 0000
     * set = set.setByte4(4, 0b1011)
     * println(set.toString()) // will print "101011". 10 + 1011
     * ```
     *
     * @param index offset for set data
     * @param value new value
     */
    fun updateByte4(index: Int, value: Byte): BitArray

    /**
     * Returns byte from 8 bites using [index]
     *
     * Example:
     * ```
     * val set = Bitset32(0b110101)// 1101 + 01
     * println(set.getByte(6)) //will print "1101". 6 offset = 4 bits of byte + 2 offset
     * ```
     *
     * @param index Offset for getting byte
     */
    fun getByte8(index: Int): Byte
}
