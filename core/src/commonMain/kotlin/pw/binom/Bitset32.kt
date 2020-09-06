package pw.binom

/**
 * Implements bitset based on [Int]
 *
 * Example:
 * ```
 * var data = Bitset32()
 * assertFalse(data[0])
 * assertFalse(data[1])
 * data=data.set(0,true)
 * assertTrue(data[0])
 * assertFalse(data[1])
 * ```
 */
inline class Bitset32(private val value: Int = 0) {
    operator fun get(index: Int): Boolean = value and (1 shl index) != 0
    fun set(index: Int, value: Boolean) =
            Bitset32(if (value)
                (this.value or (1 shl index))
            else
                (this.value.inv() or (1 shl index)).inv()
            )

    fun toInt() = value
}