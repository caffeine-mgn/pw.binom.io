package pw.binom

import kotlin.experimental.and
import kotlin.test.Test
import kotlin.test.assertEquals

class TestUtils {

    @Test
    fun testShort() {
        val value = 258.toShort()
        assertEquals(1, value[0])
        assertEquals(2, value[1])

        assertEquals(value, Short.fromBytes(1, 2))
    }

    @Test
    fun testInt() {
        val value = 16909060

        assertEquals(1, value[0])
        assertEquals(2, value[1])
        assertEquals(3, value[2])
        assertEquals(4, value[3])

        assertEquals(value, Int.fromBytes(1, 2, 3, 4))

        val value2 = 8081
        assertEquals(value2, Int.fromBytes(value2[0], value2[1], value2[2], value2[3]))
    }

    @Test
    fun testLong() {
        val value = 72623859790382856L
        assertEquals(1, value[0])
        assertEquals(2, value[1])
        assertEquals(3, value[2])
        assertEquals(4, value[3])
        assertEquals(5, value[4])
        assertEquals(6, value[5])
        assertEquals(7, value[6])
        assertEquals(8, value[7])
        assertEquals(value, Long.fromBytes(1, 2, 3, 4, 5, 6, 7, 8))
    }

    @Test
    fun testLong2(){
        val value = 336592112L
        val r = ByteArray(8)
        value.toBytes(r)

        assertEquals(0,value[0])
        assertEquals(0,value[1])
        assertEquals(0,value[2])
        assertEquals(0,value[3])
        assertEquals(20,value[4])
        assertEquals(15,value[5])
        assertEquals(-4,value[6])
        assertEquals(-16,value[7])
        r.forEachIndexed { index, byte ->
            assertEquals(value[index],byte)
        }
        assertEquals(value, Long.fromBytes(r))
        assertEquals(value, Long.fromBytes(r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7]))
    }
}