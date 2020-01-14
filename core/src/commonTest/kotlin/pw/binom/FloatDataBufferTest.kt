package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals

class FloatDataBufferTest {
    @Test
    fun test() {
        println("Endian: ${Environment.isBigEndian}")
        val d = FloatDataBuffer.alloc(10)
        d[5] = 127f
        assertEquals(127f, d[5])
        d.close()
    }
}