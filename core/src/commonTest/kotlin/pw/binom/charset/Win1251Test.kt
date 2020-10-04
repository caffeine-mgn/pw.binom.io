package pw.binom.charset

import pw.binom.*
import pw.binom.io.use
import kotlin.test.Test
import kotlin.test.assertEquals

class Win1251Test {

    private val charset = Charsets.get("WINDOWS-1251")

    @Test
    fun encode() {
        charset.newEncoder().use { encoder ->
            val out = ByteBuffer.alloc(30)
            assertEquals(CharsetTransformResult.SUCCESS, encoder.encode(test_data_hello_text.toCharBuffer(), out))
            out.flip()
            assertEquals(test_data_hello_bytes_windows_1251.size, out.remaining)

            out.forEachIndexed { index, value ->
                assertEquals(test_data_hello_bytes_windows_1251[index], value)
            }
        }
    }

    @Test
    fun encodeOutputOver() {
        charset.newEncoder().use { encoder ->
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
    }

    @Test
    fun decode() {
        charset.newDecoder().use { decoder ->
            val out = CharBuffer.alloc(30)
            assertEquals(CharsetTransformResult.SUCCESS, decoder.decode(test_data_hello_bytes_windows_1251.input(), out))
            out.flip()
            assertEquals(out.remaining, test_data_hello_text.length)
            out.forEachIndexed { index, value ->
                assertEquals(test_data_hello_text[index], value)
            }
        }
    }
}