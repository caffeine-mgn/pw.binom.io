package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BytesBitsetTest : BaseBitArrayTest() {

    @Test
    fun test() {
        val set = BytesBitArray(ByteArray(16))

        assertEquals(16 * 8, set.size)
        assertFalse(set[0])
        set[0] = true
        println("data: $set")
        assertTrue(set[0])

        assertFalse(set[36])
        set[36] = true
        assertTrue(set[36])

        assertFalse(set[1])
        set[1] = true
        println(set)
        assertTrue(set[1])
    }

    override fun makeNew() = BytesBitArray(ByteArray(4))
    override fun toStringTest() {
        val b = makeNew()
        b[0] = true
        b[1] = true
        b[3] = true
        b[7] = true
        assertEquals("11010001000000000000000000000000", b.toString())
    }
}
