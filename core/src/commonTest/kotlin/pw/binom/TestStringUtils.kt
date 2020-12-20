package pw.binom

import pw.binom.charset.Charsets
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Russian word "Привет" in charset windows-1251. Translated as "Hi"
 */
val test_data_hello_bytes_windows_1251 = byteArrayOf(
    (-49).toByte(),
    (-16).toByte(),
    (-24).toByte(),
    (-30).toByte(),
    (-27).toByte(),
    (-14).toByte()
)
const val test_data_hello_text = "Привет"
val test_data_hello_bytes_utf_8 = test_data_hello_text.encodeToByteArray()

class TestStringUtils {

    @Test
    fun `numbers`() {
        val data = "123456".encodeBytes()
        assertEquals(6, data.size)
    }

    @Test
    fun `latin`() {
        val data = "ABCDEF".encodeBytes()
        assertEquals(6, data.size)
    }

    @Test
    fun `russian`() {
        val data = "АБВ".encodeBytes()
        assertEquals(6, data.size)
    }

    @Test
    fun encodeTest() {
        val data = test_data_hello_text.encodeBytes(Charsets.get("windows-1251"))

        assertEquals(test_data_hello_bytes_windows_1251.size, data.size, "Invalid size of data")
        data.forEachIndexed { index, byte ->
            assertEquals(test_data_hello_bytes_windows_1251[index], byte, "Fail on char $index")
        }
    }

    @Test
    fun decodeTest() {
        assertEquals(
            test_data_hello_text,
            test_data_hello_bytes_windows_1251.decodeString(Charsets.get("windows-1251"))
        )
    }

    @Test
    fun test2() {
        val data = byteArrayOf(97, 112, 112, 108, 105, 99, 97, 116, 105, 111, 110, 95, 110, 97, 109, 101)
        assertEquals("application_name", data.decodeString(Charsets.UTF8))
    }

    @Test
    fun decodeEmpty() {
        assertEquals("", byteArrayOf().decodeString())
    }
}