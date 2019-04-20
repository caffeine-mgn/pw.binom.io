package pw.binom.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TestByteArrayInputStream {

    @Test
    fun `read test`() {
        val data = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        val buf = ByteArray(5)
        val stream = ByteArrayInputStream(data)

        assertEquals(5, stream.read(buf))
        for (i in 0 until 5) {
            assertEquals(data[i], buf[i])
        }
    }

    @Test
    fun `invalid buffer array`() {
        val data = ByteArray(3)
        val stream = ByteArrayInputStream(data)
        val buf = ByteArray(5)
        try {
            stream.read(buf, 11, 12)
            fail()
        } catch (e: IndexOutOfBoundsException) {
            assertEquals("Range [11, 11 + 12) out of bounds for length 5", e.message)
            //NOP
        }
    }

    @Test
    fun `invalid input array`() {
        val data = ByteArray(5)
        try {
            ByteArrayInputStream(data, 11, 12)
            fail()
        } catch (e: IndexOutOfBoundsException) {
            assertEquals("Range [11, 11 + 12) out of bounds for length 5", e.message)
            //NOP
        }
    }

    @Test
    fun `mutable input`() {
        val data = ByteArray(3)
        val stream = ByteArrayInputStream(data)

        data[0] = 1
        val buf = ByteArray(5)
        stream.read(buf)

        assertEquals(data[0], buf[0])
    }

}