package pw.binom.io.socket.ssl

import pw.binom.io.MD5
import kotlin.test.Test
import kotlin.test.assertEquals

class MD5Test {

    @Test
    fun test() {
        val m = MD5()
        m.init()
        m.update("123".encodeToByteArray())
        val result = m.finish()
        assertEquals(0x20.toByte(), result[0])
        assertEquals(0x2c.toByte(), result[1])
        assertEquals(0xb9.toByte(), result[2])
        assertEquals(0x62.toByte(), result[3])
        assertEquals(0xac.toByte(), result[4])
        assertEquals(0x59.toByte(), result[5])
        assertEquals(0x07.toByte(), result[6])
        assertEquals(0x5b.toByte(), result[7])
        assertEquals(0x96.toByte(), result[8])
        assertEquals(0x4b.toByte(), result[9])
        assertEquals(0x07.toByte(), result[10])
        assertEquals(0x15.toByte(), result[11])
        assertEquals(0x2d.toByte(), result[12])
        assertEquals(0x23.toByte(), result[13])
        assertEquals(0x4b.toByte(), result[14])
        assertEquals(0x70.toByte(), result[15])
    }
}