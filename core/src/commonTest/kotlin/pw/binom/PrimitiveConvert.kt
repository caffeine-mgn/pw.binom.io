package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimitiveConvert {

    @Test
    fun testInt() {
        val value = 8081
        val result = Int.fromBytes(value[0], value[1], value[2], value[3])
        assertEquals(value, result)
    }
}
