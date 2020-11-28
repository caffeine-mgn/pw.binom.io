package pw.binom.io

import pw.binom.ByteDataBuffer
import pw.binom.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class TestBufferedInput {

    @Test
    fun test() {
        val data = ByteBuffer.alloc(256)
        Random.nextBytes(data)
        data.clear()
        val b = data.bufferedInput(10)

        val buf = ByteBuffer.alloc(50)
        assertEquals(-1, b.available)
        buf.reset(0,5)
        b.read(buf)
        assertEquals(5, b.available)
        assertArrayEquals(data, 0, buf, 0, 5)
        buf.reset(0,5)
        b.read(buf)
        assertArrayEquals(data, 5, buf, 0, 5)
        buf.reset(0,10)
        b.read(buf)
        assertArrayEquals(data, 10, buf, 0, 5)
    }
}

fun assertArrayEquals(expected: ByteDataBuffer, expectedOffset: Int, actual: ByteDataBuffer, actualOffset: Int, length: Int) {
    for (i in expectedOffset until expectedOffset + length) {
        assertEquals(expected[i], actual[i - expectedOffset + actualOffset], "On Element ${i - expectedOffset}")
    }
}

fun assertArrayEquals(expected: ByteBuffer, expectedOffset: Int, actual: ByteBuffer, actualOffset: Int, length: Int) {
    for (i in expectedOffset until expectedOffset + length) {
        assertEquals(expected[i], actual[i - expectedOffset + actualOffset], "On Element ${i - expectedOffset}")
    }
}