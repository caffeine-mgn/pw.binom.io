package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BytesBitsetTest {

    @Test
    fun test() {
        val array = ByteArray(16)
        val set = BytesBitArray(array)

        assertEquals(16 * 8, set.size)
        assertFalse(set[0])
        set[0] = true
        assertTrue(set[0])

        assertFalse(set[36])
        set[36] = true
        assertTrue(set[36])
        assertEquals(0b1, array[0].toInt() and 0xFF)

        assertFalse(set[1])
        set[1] = true
        assertTrue(set[1])
        assertEquals(0b11, array[0].toInt() and 0xFF)
    }
}