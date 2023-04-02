package pw.binom.charset

import pw.binom.Environment
import pw.binom.Platform
import pw.binom.platform
import kotlin.test.Test
import kotlin.test.assertEquals

class CharsetCoderTest {

    @Test
    fun encodeTest() {
        if (Environment.platform != Platform.JS) {
            return
        }
        val charsetCoder = CharsetCoder(Charsets.get("WINDOWS-1251"))

        val data = charsetCoder.encode(test_data_hello_text) { it.toByteArray() }
        assertEquals(test_data_hello_bytes_windows_1251.size, data.size)
        data.forEachIndexed { index, value ->
            assertEquals(test_data_hello_bytes_windows_1251[index], value)
        }
    }

    @Test
    fun decodeTest() {
        if (Environment.platform != Platform.JS) {
            return
        }
        val charsetCoder = CharsetCoder(Charsets.get("WINDOWS-1251"))
        assertEquals(test_data_hello_text, charsetCoder.decode(test_data_hello_bytes_windows_1251))
    }
}
