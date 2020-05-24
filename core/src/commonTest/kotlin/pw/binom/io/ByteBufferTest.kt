package pw.binom.io

import kotlin.test.Test
import kotlin.test.assertEquals

class ByteBufferTest {

    @Test
    fun test() {
        val v = ByteBuffer(64)
        v.write(ByteArray(244))
        assertEquals(244, v.readRemaining)
        assertEquals(200, v.read(ByteArray(200)))
        assertEquals(44, v.readRemaining)
    }
}