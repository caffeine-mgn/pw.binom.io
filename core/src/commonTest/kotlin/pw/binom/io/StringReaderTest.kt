package pw.binom.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StringReaderTest {

    @Test
    fun `read`() {
        val reader = "123".asReader()

        assertEquals('1', reader.read())
        assertEquals('2', reader.read())
        assertEquals('3', reader.read())

        assertNull(reader.read())
    }

    @Test
    fun `out of length`() {
        val reader = "1234567890".asReader()

        val data = CharArray(20)

        reader.read(data, length = 5)
        assertEquals(5, reader.read(data, length = 10))
        assertEquals(0, reader.read(data))
    }
}