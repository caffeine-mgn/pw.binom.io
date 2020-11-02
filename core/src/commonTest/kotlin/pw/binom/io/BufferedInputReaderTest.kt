package pw.binom.io

import pw.binom.*
import pw.binom.charset.Charsets
import kotlin.test.*

class BufferedInputReaderTest {
    @Test
    fun readByCharTest() {
        val input = ByteBuffer.wrap(test_data_hello_bytes_windows_1251)
        val bufPool = ByteBufferPool(2)
        val reader = BufferedInputReader(Charsets.get("windows-1251"), input, bufPool)
        test_data_hello_text.forEachIndexed { index, c ->
            assertEquals(c, assertNotNull(reader.read()), "Fail in index $index")
        }
        assertNull(reader.read())
    }

    @Test
    fun readByLineTest() {
        val bufPool = ByteBufferPool(2)
        val input = ByteBuffer.wrap(test_data_hello_bytes_windows_1251)
        val reader = BufferedInputReader(Charsets.get("windows-1251"), input, bufPool)
        val output = CharArray(test_data_hello_text.length)
        assertEquals(test_data_hello_text.length, reader.read(output))
        output.forEachIndexed { index, c ->
            println("$index -> $c")
        }
        test_data_hello_text.forEachIndexed { index, c ->
            assertEquals(c, assertNotNull(output[index]), "Fail in index $index")
        }
        assertNull(reader.read())
    }
}