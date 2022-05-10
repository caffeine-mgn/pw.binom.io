package pw.binom.concurrency

import pw.binom.collection.FrozenHashMap
import kotlin.test.Test
import kotlin.test.assertEquals

class FrozenHashMapTest {
    @Test
    fun put() {
        val map = FrozenHashMap<Int, String>()
        map[1] = "ololo"
        assertEquals(1, map.size)
        assertEquals("ololo", map[1])
    }
}
