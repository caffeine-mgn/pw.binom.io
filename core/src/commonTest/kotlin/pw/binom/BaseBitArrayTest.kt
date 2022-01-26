package pw.binom

import kotlin.test.*

abstract class BaseBitArrayTest {
    protected abstract fun makeNew(): BitArray

    @Test
    abstract fun toStringTest()

    @Ignore
    @Test
    fun getSetByte4Test() {
        var set = makeNew()
        set = set.update(0, true)
        set = set.update(1, true)
        set = set.update(31, true)
        set = set.updateByte4(2, 0b0111)

        assertEquals(0b0111, set.getByte4(2))

        set = makeNew()
        set = set.updateByte4(2, 0b1111)
        assertEquals(0b1111, set.getByte4(2))
    }

    @Test
    fun getByte8Test() {
        var array = makeNew().update(0, true)
        assertEquals(-128, array.getByte8(0))
        array = makeNew().update(1, true)
        assertEquals(-128, array.getByte8(1))
    }

    @Test
    fun getByte4Test() {
        var array = makeNew().update(0, true)
        assertEquals(8, array.getByte4(0))
        array = makeNew().update(4, true)
        assertEquals(8, array.getByte4(4))

        array = makeNew().update(3, true)
        println(array)
        assertEquals(8, array.getByte4(3))

        array = makeNew().update(5, true)
        println(array)
        assertEquals(8, array.getByte4(5))
    }

    @Test
    fun setByte4Test() {
        fun testOffset(offset: Int) {
            try {
                val arr = makeNew().updateByte4(offset, 0b1011)
                assertTrue(arr[0 + offset])
                assertFalse(arr[1 + offset])
                assertTrue(arr[2 + offset])
                assertTrue(arr[3 + offset])
            } catch (e: Throwable) {
                throw RuntimeException("Error on offset $offset", e)
            }
        }
//        testOffset(1)
        repeat(5) {
            testOffset(it)
        }
    }
}
