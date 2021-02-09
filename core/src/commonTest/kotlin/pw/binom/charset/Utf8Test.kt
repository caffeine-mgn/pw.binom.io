package pw.binom.charset

import pw.binom.*
import pw.binom.io.use
import kotlin.test.Test
import kotlin.test.assertEquals

class Utf8Test {

    private val charset = Charsets.get("UTF-8")

    @Test
    fun encode() {
        charset.newEncoder().use { encoder ->
            val out = ByteBuffer.alloc(30)
            assertEquals(CharsetTransformResult.SUCCESS, encoder.encode(test_data_hello_text.toCharBuffer(), out))
            out.flip()
            assertEquals(out.remaining, test_data_hello_bytes_utf_8.size)

            out.forEachIndexed { index, value ->
                assertEquals(test_data_hello_bytes_utf_8[index], value)
            }
        }
    }

    @Test
    fun encodeOutputOver() {
        charset.newEncoder().use { encoder ->
            val out = ByteBuffer.alloc(2)
            val input = test_data_hello_text.toCharBuffer()
            assertEquals(CharsetTransformResult.OUTPUT_OVER, encoder.encode(input, out))
            assertEquals(2, out.position)
            assertEquals(1, input.position)
        }
    }

    @Test
    fun decode() {
        charset.newDecoder().use { encoder ->
            val out = CharBuffer.alloc(30)
            assertEquals(CharsetTransformResult.SUCCESS, encoder.decode(test_data_hello_text.encodeToByteArray().wrap(), out))
            out.flip()
            assertEquals(out.remaining, test_data_hello_text.length)
            out.forEachIndexed { index, value ->
                assertEquals(test_data_hello_text[index], value)
            }
        }
    }
}