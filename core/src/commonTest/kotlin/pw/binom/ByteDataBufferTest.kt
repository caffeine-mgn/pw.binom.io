package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals

class ByteDataBufferTest {

    @Test
    fun test() {
        val d = ByteDataBuffer.alloc(10)
        d[5] = 127
        assertEquals(127, d[5])
    }
}