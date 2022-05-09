package pw.binom

import pw.binom.utils.TreeMap
import kotlin.test.Test
import kotlin.test.assertEquals

class TreeMapTest {
    @Test
    fun test() {
        val m = TreeMap<Int, Int>()
        repeat(100) {
            m[it] = it
        }
        m[200] = 1
        assertEquals(1, m[200])
    }
}
