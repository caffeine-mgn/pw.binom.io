package pw.binom.io

import pw.binom.encodeBytes
import kotlin.test.Test
import kotlin.test.assertEquals

class TestCRC32 {

    @Test
    fun `CRC-32`() {
        val data = "1".encodeBytes()

        val crc = CRC32()

        crc.update(data)
        assertEquals(crc.value, 0x83DCEFB7u)
    }

    @Test
    fun `CRC-32C`() {
        val data = "1".encodeBytes()

        val crc = CRC32C()

        crc.update(data)
        assertEquals(crc.value, 0x90F599E3u)
    }
}