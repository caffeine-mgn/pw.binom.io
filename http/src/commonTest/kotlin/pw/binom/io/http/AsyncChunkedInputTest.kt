package pw.binom.io.http

import pw.binom.*
import pw.binom.io.ByteArrayOutput
import pw.binom.io.readText
import pw.binom.io.utf8Appendable
import pw.binom.io.utf8Reader
import kotlin.test.Test
import kotlin.test.assertEquals

class AsyncChunkedInputTest {

    @Test
    fun test2(){
        val output = ByteArrayOutput()
        val chunked = AsyncChunkedOutput(output.asyncOutput())

        async {
            val sb = chunked.utf8Appendable()
            sb.append("Wiki")
            chunked.flush()
            sb.append("pedia")
            sb.asyncClose()

            val out = output.data
            out.flip()

            val input = AsyncChunkedInput(out.asyncInput())
            assertEquals("Wikipedia",input.utf8Reader().readText())
        }
    }

    @Test
    fun test() {
        val output = ByteArrayOutput()
        val chunked = AsyncChunkedOutput(output.asyncOutput())

        async {
            val sb = chunked.utf8Appendable()
            sb.append("Wiki")
            chunked.flush()
            sb.append("pedia")
            sb.asyncClose()
        }
        val out = output.data
        out.flip()

        val input = AsyncChunkedInput(out.asyncInput())
        val buf = ByteBuffer.alloc(50)
        val buf1 = ByteBuffer.alloc(4)
        async {
            assertEquals(4, input.read(buf))
            assertEquals('W', buf.readUtf8Char(buf1))
            assertEquals('i', buf.readUtf8Char(buf1))
            assertEquals('k', buf.readUtf8Char(buf1))
            assertEquals('i', buf.readUtf8Char(buf1))
            buf.clear()
            assertEquals(5, input.read(buf))
            assertEquals('p', buf.readUtf8Char(buf1))
            assertEquals('e', buf.readUtf8Char(buf1))
            assertEquals('d', buf.readUtf8Char(buf1))
            assertEquals('i', buf.readUtf8Char(buf1))
            assertEquals('a', buf.readUtf8Char(buf1))
            buf.clear()
            assertEquals(0, input.read(buf))
        }
    }
}