package pw.binom.crc

import kotlin.test.Test
import kotlin.test.assertEquals

class TestCRC32 {

  @Test
  fun CRC_32() {
    val data = "1".encodeToByteArray()

    val crc = CRC32()

    crc.update(data)
    assertEquals(crc.value, 0x83DCEFB7u)
  }

  @Test
  fun CRC_32C() {
    val data = "1".encodeToByteArray()

    val crc = CRC32C()

    crc.update(data)
    assertEquals(crc.value, 0x90F599E3u)
  }
}
