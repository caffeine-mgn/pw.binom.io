package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Bitset32Test : BaseBitArrayTest() {

    @Test
    fun test() {
        var bit = BitArray32()
        assertEquals("0", bit.toUInt().toString(2))
        bit = bit.update(0, true)
        assertTrue(bit[0])
        (1..31).forEach {
            assertFalse("Index=$it") { bit[it] }
        }
        assertEquals("10000000000000000000000000000000", bit.toUInt().toString(2))

        bit = bit.update(1, true)
        assertTrue(bit[0])
        assertTrue(bit[1])
        assertEquals("11000000000000000000000000000000", bit.toUInt().toString(2))

        (2..31).forEach {
            assertFalse("Index=$it") { bit[it] }
        }
    }

    override fun toStringTest() {
        var set = BitArray32()
        set = set.update(0, true)
        set = set.update(1, true)
        set = set.update(31, true)
        assertEquals("11000000000000000000000000000001", set.toString())
    }

//    @Test
    fun getSetByte4Test1() {
        var set = BitArray32()
        set = set.update(0, true)
        set = set.update(1, true)
        set = set.update(31, true)
        set = set.updateByte4(2, 0b0111)

        assertEquals("11011100000000000000000000000001", set.toUInt().toString(2))
        assertEquals(0b0111, set.getByte4(2))

        set = BitArray32()
        set = set.updateByte4(2, 0b1111)
        assertEquals("00111100000000000000000000000000", set.toString())

        assertEquals(0b1111, set.getByte4(2))
//        println("Result: 0b${set.toInt().toString(2)}")
//        assertEquals(0b1011, set.toInt())
//        assertEquals(0b1011, set.getByte4(4))
//
//        set = Bitset32(0)
//        set = set.set(0, true)
//        set = set.set(7, true)
//
//        set = set.setByte4(6, 0b1011)
//        assertEquals(set.toInt(), 0b10101101)
    }

    override fun makeNew(): BitArray = BitArray32()
}
