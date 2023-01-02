package pw.binom.compression.tar

import pw.binom.io.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals

class UtilsTest {

    @Test
    fun octTest() {
        val original = "0100777".toUInt()
        val d = ByteBuffer(15)
        original.toOct(d, 0, 15)
        assertEquals(original, d.oct2ToUInt(0, 15))
    }
}
