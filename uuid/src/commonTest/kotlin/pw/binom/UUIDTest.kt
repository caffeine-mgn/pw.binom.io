package pw.binom

import pw.binom.uuid.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class UUIDTest {
    @Test
    fun test() {
        val s = UUID.random()
        val n = UUID.fromString(s.toString())
        assertEquals(s, n)
        println("->\"$s\"   ${s.toShortString()}")

        val data = ByteArray(16)
        s.toByteArray(data)

        val b = UUID.create(data)
        assertEquals(s, b)
    }
}
