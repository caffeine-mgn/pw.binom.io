package pw.binom

import kotlin.test.Test
import kotlin.test.assertEquals

class TestStringUtils {

    @Test
    fun `numbers`() {
        val data = "123456".asUTF8ByteArray()
        assertEquals(6, data.size)
    }

    @Test
    fun `latin`() {
        val data = "ABCDEF".asUTF8ByteArray()
        assertEquals(6, data.size)
    }

    @Test
    fun `russian`() {
        val data = "АБВ".asUTF8ByteArray()
        assertEquals(6, data.size)
    }
}