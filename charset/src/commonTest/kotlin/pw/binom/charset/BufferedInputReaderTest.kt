package pw.binom.charset

import pw.binom.*
import pw.binom.io.BufferedInputReader
import pw.binom.io.ByteBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BufferedInputReaderTest {
    @Test
    fun readByCharTest() {
        if (Environment.platform != Platform.JS) {
            return
        }
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
        if (Environment.platform != Platform.JS) {
            return
        }
        val bufPool = ByteBufferPool(10)
        val input = ByteBuffer.wrap(test_data_hello_bytes_windows_1251)
        val reader =
            BufferedInputReader(charset = Charsets.get("windows-1251"), input = input, pool = bufPool)
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
