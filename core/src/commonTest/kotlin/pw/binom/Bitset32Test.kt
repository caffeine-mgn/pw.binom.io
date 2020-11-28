package pw.binom

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Bitset32Test {

    @Test
    fun test() {
        var bit = Bitset32()
        bit = bit.set(0, true)
        assertTrue(bit[0])
        (1..31).forEach {
            assertFalse("Index=$it") { bit[it] }
        }

        bit = bit.set(1, true)
        assertTrue(bit[0])
        assertTrue(bit[1])

        (2..31).forEach {
            assertFalse("Index=$it") { bit[it] }
        }
    }
}