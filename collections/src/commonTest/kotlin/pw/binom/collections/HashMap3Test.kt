package pw.binom.collections

import kotlin.test.*

class HashMap3Test {
    @Test
    fun test3() {
        val v = defaultHashMap<Int, Boolean>()
        v[10] = true
        v[12] = true
        val it = v.iterator()
        assertTrue(it.hasNext())
        it.next()
        assertTrue(it.hasNext())
        it.next()
        assertFalse(it.hasNext())

        assertEquals(true, v[10])
        assertNull(v[11])
        assertFalse(v.containsKey(11))

        v.toList().also {
            assertEquals(2, it.size)
            assertTrue(it.any { it.first == 10 && it.second })
            assertTrue(it.any { it.first == 12 && it.second })
        }
    }

    @Test
    fun test5() {
        val dd = defaultHashMap<String, String>()

        dd["993"] = "993"
        dd["981"] = "981"
        dd["968"] = "968"
        dd.asSequence().toList().also {
            assertEquals(3, it.size)
            assertEquals("968", it[0].key)
            assertEquals("968", it[0].value)

            assertEquals("981", it[1].key)
            assertEquals("981", it[1].value)

            assertEquals("993", it[2].key)
            assertEquals("993", it[2].value)
        }
    }

    @Test
    fun test4() {
        val dd = defaultHashMap<String, String>()
        dd["database.sqlite.file"] = "index.db"
        dd["external_http.0.port"] = "8080"

        dd.entries.toList().also {
            assertTrue(it.any { it.key == "external_http.0.port" })
            assertTrue(it.any { it.key == "database.sqlite.file" })
        }
    }
}
