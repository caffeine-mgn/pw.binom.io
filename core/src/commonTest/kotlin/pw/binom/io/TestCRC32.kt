package pw.binom.io

import pw.binom.asUTF8ByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class TestCRC32 {

    @Test
    fun test() {
        val data = "1".asUTF8ByteArray()

        val crc = CRC32()

        crc.update(data)
        assertEquals(crc.value,2212294583u)
    }
}