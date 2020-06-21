package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.async
import pw.binom.asyncInput
import kotlin.test.Test
import kotlin.test.assertEquals

class TestAsyncBufferedInput {

    @Test
    fun test() {
        val data = ByteDataBuffer.alloc(256)
        (0 until 256).forEach {
            data[it] = it.toByte()
        }
        val b = data.toInput().asyncInput().bufferedInput(10)

        async {
            val buf = ByteDataBuffer.alloc(50)
            assertEquals(-1, b.available)
            assertEquals(5, b.readFully(buf, length = 5))
            assertEquals(5, b.available)
            assertArrayEquals(data, 0, buf, 0, 5)
            assertEquals(5, b.readFully(buf, length = 5))
            assertArrayEquals(data, 5, buf, 0, 5)
            assertEquals(10, b.readFully(buf, length = 10))
            assertArrayEquals(data, 10, buf, 0, 5)
        }
    }
}