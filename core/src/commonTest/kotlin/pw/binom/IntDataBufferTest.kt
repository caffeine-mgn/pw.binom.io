package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals

class IntDataBufferTest{
    @Test
    fun test() {
        val d = IntDataBuffer.alloc(10)
        d[5] = 127
        assertEquals(127, d[5])
        d.close()
    }
}