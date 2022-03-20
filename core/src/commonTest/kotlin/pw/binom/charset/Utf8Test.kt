package pw.binom.charset

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import pw.binom.*
import pw.binom.concurrency.sleep
import pw.binom.io.*
import kotlin.test.Ignore
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
        test_data_hello_text.encodeToByteArray().wrap { buffer ->
            assertEquals(
                CharsetTransformResult.SUCCESS,
                encoder.decode(buffer, out)
            )
            assertEquals(0, buffer.remaining)
        }
        out.flip()
        assertEquals(out.remaining, test_data_hello_text.length)
        out.forEachIndexed { index, value ->
            assertEquals(test_data_hello_text[index], value)
        }
    }

    @Test
    fun test() {
        val out = ByteArrayOutput()

        val d = GlobalScope.async {
            out.asyncOutput().bufferedWriter(charset = Charsets.UTF8, closeParent = false).use {
//                it.append("\uD83E\uDC08")
                it.append("🠈")
                it.flush()
            }
        }
        while (!d.isCompleted) {
            sleep(100)
        }
        if (d.getCompletionExceptionOrNull() != null) {
            throw d.getCompletionExceptionOrNull()!!
        }
        out.trimToSize()
        out.data.flip()
        assertEquals("\uD83E\uDC08", out.data.toByteArray().decodeToString())
    }

    @Ignore
    @Test
    fun ddd() {
        val emoji = "\uD83D\uDE0A"
        val emojiBytes = emoji.encodeToByteArray()
        println("->string.length=${emoji.length}, bytes.length=${emojiBytes.size}")
        val vv = ByteBuffer.wrap(emojiBytes).use {
            it.bufferedReader().readText()
        }
        println("iconv---->${vv == emoji}")
        val readed = ByteBuffer.wrap(emojiBytes).use {
            it.utf8Reader().readText()
        }
//        println("main---->${readed==emoji}")
//        println("0=${emojiBytes[0].toString(16)}")
//        println("1=${emojiBytes[1].toString(16)}")
//        println("2=${emojiBytes[2].toString(16)}")
//        println("3=${emojiBytes[3].toString(16)}")
        val inFact = ByteBuffer.alloc(6) {
            UTF8.unicodeToUtf8(emoji, it)
            it.clear()
            it.toByteArray()
        }
        println()
    }
}
