package pw.binom.io

import kotlinx.coroutines.test.runTest
import pw.binom.asyncInput
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class TestAsyncBufferedInput {

    @Test
    fun test() = runTest {
        val data = ByteBuffer.alloc(256)
        Random.nextBytes(data)
        data.clear()
        val b = data.asyncInput().bufferedInput(10)

        val buf = ByteBuffer.alloc(50)
        assertEquals(-1, b.available)
        buf.reset(0, 5)
        assertEquals(5, b.readFully(buf))
        assertEquals(5, b.available)
        assertArrayEquals(data, 0, buf, 0, 5)
        buf.reset(0, 5)
        assertEquals(5, b.readFully(buf))
        assertArrayEquals(data, 5, buf, 0, 5)
        buf.reset(0, 10)
        assertEquals(10, b.readFully(buf))
        assertArrayEquals(data, 10, buf, 0, 5)
    }
}

fun assertArrayEquals(expected: ByteBuffer, expectedOffset: Int, actual: ByteBuffer, actualOffset: Int, length: Int) {
    for (i in expectedOffset until expectedOffset + length) {
        assertEquals(expected[i], actual[i - expectedOffset + actualOffset], "On Element ${i - expectedOffset}")
    }
}
