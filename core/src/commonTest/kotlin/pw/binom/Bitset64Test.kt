package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Bitset64Test : BaseBitArrayTest() {

    @Test
    fun test() {
        var bit = BitArray64()
        assertEquals("0", bit.toULong().toString(2))
        bit = bit.update(0, true)
        assertTrue(bit[0])
        (1..31).forEach {
            assertFalse("Index=$it") { bit[it] }
        }
        assertEquals("1000000000000000000000000000000000000000000000000000000000000000", bit.toULong().toString(2))

        bit = bit.update(1, true)
        assertTrue(bit[0])
        assertTrue(bit[1])
        assertEquals("1100000000000000000000000000000000000000000000000000000000000000", bit.toULong().toString(2))

        (2..31).forEach {
            assertFalse("Index=$it") { bit[it] }
        }
    }

    override fun toStringTest() {
        var set = BitArray64()
        set = set.update(0, true)
        set = set.update(1, true)
        set = set.update(31, true)
        set = set.update(61, true)
        assertEquals("1100000000000000000000000000000100000000000000000000000000000100", set.toString())
    }

    @Test
    fun ff() {
        var set = BitArray64()
//        set = set.set(0, true)
//        set = set.set(1, true)
//        set = set.set(31, true)
        set = set.updateByte4(3, 0xF.toByte())
        println("->$set")
    }
    override fun makeNew(): BitArray = BitArray64()
}
