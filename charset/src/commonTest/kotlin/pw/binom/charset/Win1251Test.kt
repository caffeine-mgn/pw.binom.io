package pw.binom.charset

import pw.binom.*
import kotlin.test.Test
import kotlin.test.assertEquals

class Win1251Test {

    private val charset = Charsets.get("WINDOWS-1251")

    @Test
    fun encode() {
        val encoder = charset.newEncoder()
        val out = ByteBuffer.alloc(30)
        assertEquals(CharsetTransformResult.SUCCESS, encoder.encode(test_data_hello_text.toCharBuffer(), out))
        out.flip()
        assertEquals(test_data_hello_bytes_windows_1251.size, out.remaining123)

        out.forEachIndexed { index, value ->
            assertEquals(test_data_hello_bytes_windows_1251[index], value)
        }
    }

    @Test
    fun encodeOutputOver() {
        val encoder = charset.newEncoder()
        val out = ByteBuffer.alloc(30)
        out.limit = 2
        val input = test_data_hello_text.toCharBuffer()
        assertEquals(CharsetTransformResult.OUTPUT_OVER, encoder.encode(input, out))
        assertEquals(2, out.position)
        assertEquals(2, input.position)
        out.limit = out.capacity
        assertEquals(CharsetTransformResult.SUCCESS, encoder.encode(input, out))
        assertEquals(6, out.position)
    }

    @Test
    fun decode() {
        val decoder = charset.newDecoder()
        val out = CharBuffer.alloc(30)
        test_data_hello_bytes_windows_1251.wrap { buffer ->
            assertEquals(
                CharsetTransformResult.SUCCESS,
                decoder.decode(buffer, out)
            )
            assertEquals(0, buffer.remaining123)
        }
        out.flip()
        assertEquals(out.remaining123, test_data_hello_text.length)
        out.forEachIndexed { index, value ->
            assertEquals(test_data_hello_text[index], value)
        }
    }
}
