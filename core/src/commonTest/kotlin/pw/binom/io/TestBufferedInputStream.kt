package pw.binom.io

import kotlin.test.Test
import kotlin.test.assertEquals

class TestBufferedInputStream {

    @Test
    fun test() {
        val data = ByteArray(256) { it.toByte() }
        val b = data.toInputStream().buffered(10)

        val buf = ByteArray(50)
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

fun assertArrayEquals(expected: ByteArray, expectedOffset: Int, actual: ByteArray, actualOffset: Int, length: Int) {
    for (i in expectedOffset until expectedOffset + length) {
        assertEquals(expected[i], actual[i - expectedOffset + actualOffset], "On Element ${i - expectedOffset}")
    }
}