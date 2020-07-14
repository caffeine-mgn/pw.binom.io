package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.alloc
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
/*
class TestByteArrayInput {

    @Test
    fun `read test`() {
        val data = ByteDataBuffer.wrap(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

        val buf = ByteDataBuffer.alloc(5)
        val stream = ByteArrayInput(data)

        assertEquals(5, stream.read(buf))
        for (i in 0 until 5) {
            assertEquals(data[i], buf[i])
        }
    }

    @Test
    fun `invalid buffer array`() {
        val data = ByteDataBuffer.alloc(3)
        val stream = ByteArrayInput(data)
        val buf = ByteDataBuffer.alloc(5)

        try {
            assertEquals(0, stream.read(buf, 11, 12))
            fail()
        } catch (e: IndexOutOfBoundsException) {
            assertEquals("Range [11, 11 + 12) out of bounds for length 5", e.message)
        }
    }

    @Test
    fun `invalid input array`() {
        val data = ByteDataBuffer.alloc(5)
        try {
            ByteArrayInput(data, 11, 12)
            fail()
        } catch (e: IndexOutOfBoundsException) {
            assertEquals("Range [11, 11 + 12) out of bounds for length 5", e.message)
            //NOP
        }
    }

    @Test
    fun `offset and limit`() {
        val b = ByteDataBuffer.alloc(5) { it.toByte() }
                .toInput(offset = 1, length = 3)

        val data = ByteDataBuffer.alloc(40)
        data.fill(0)
        assertEquals(3, b.read(data))

        data.forEachIndexed { index, byte ->
            if (index in 0..2)
                assertEquals(b.data[index + 1], byte)
            else
                assertEquals(0, byte)
        }
    }

    @Test
    fun `mutable input`() {
        val data = ByteDataBuffer.alloc(3)
        val stream = ByteArrayInput(data)

        data[0] = 1
        val buf = ByteDataBuffer.alloc(5)
        assertEquals(3, stream.read(buf))

        assertEquals(data[0], buf[0])
    }

}
*/