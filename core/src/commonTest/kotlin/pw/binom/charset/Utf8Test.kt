package pw.binom.charset

import pw.binom.*
import pw.binom.io.ByteArrayOutput
import pw.binom.io.bufferedWriter
import pw.binom.io.use
import kotlin.test.Test
import kotlin.test.assertEquals

class Utf8Test {

    private val charset = Charsets.get("UTF-8")

    @Test
    fun encode() {
        val encoder = charset.newEncoder()
            val out = ByteBuffer.alloc(30)
            assertEquals(CharsetTransformResult.SUCCESS, encoder.encode(test_data_hello_text.toCharBuffer(), out))
            out.flip()
            assertEquals(out.remaining, test_data_hello_bytes_utf_8.size)

            out.forEachIndexed { index, value ->
                assertEquals(test_data_hello_bytes_utf_8[index], value)
            }

    }

    @Test
    fun encodeOutputOver() {
        val encoder = charset.newEncoder()
            val out = ByteBuffer.alloc(2)
            val input = test_data_hello_text.toCharBuffer()
            assertEquals(CharsetTransformResult.OUTPUT_OVER, encoder.encode(input, out))
            assertEquals(2, out.position)
            assertEquals(1, input.position)

    }

    @Test
    fun decode() {
        val encoder = charset.newDecoder()
            val out = CharBuffer.alloc(30)
            assertEquals(
                CharsetTransformResult.SUCCESS,
                encoder.decode(test_data_hello_text.encodeToByteArray().wrap(), out)
            )
            out.flip()
            assertEquals(out.remaining, test_data_hello_text.length)
            out.forEachIndexed { index, value ->
                assertEquals(test_data_hello_text[index], value)

        }
    }

    @Test
    fun test() {
        val out = ByteArrayOutput()

        val d = async2 {
            out.asyncOutput().bufferedWriter(charset = Charsets.UTF8, closeParent = false).use {
//                it.append("\uD83E\uDC08")
                it.append("🠈")
                it.flush()
            }
        }
        if (d.isFailure) {
            throw d.exceptionOrNull!!
        }
        out.trimToSize()
        out.data.flip()
        assertEquals("\uD83E\uDC08", out.data.toByteArray().decodeToString())

    }
}