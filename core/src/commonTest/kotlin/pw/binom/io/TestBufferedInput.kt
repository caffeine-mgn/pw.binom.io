package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TestBufferedInput {

    @Test
    fun test() {
        val data = ByteDataBuffer.alloc(256){it.toByte()}
        val b = data.toInput().bufferedInput(10)

        val buf = ByteDataBuffer.alloc(50)
        assertEquals(-1, b.available)
        b.read(buf, length = 5)
        assertEquals(5, b.available)
        assertArrayEquals(data, 0, buf, 0, 5)
        b.read(buf, length = 5)
        assertArrayEquals(data, 5, buf, 0, 5)
        b.read(buf, length = 10)
        assertArrayEquals(data, 10, buf, 0, 5)
    }
}

fun assertArrayEquals(expected: ByteDataBuffer, expectedOffset: Int, actual: ByteDataBuffer, actualOffset: Int, length: Int) {
    for (i in expectedOffset until expectedOffset + length) {
        assertEquals(expected[i], actual[i - expectedOffset + actualOffset], "On Element ${i - expectedOffset}")
    }
}