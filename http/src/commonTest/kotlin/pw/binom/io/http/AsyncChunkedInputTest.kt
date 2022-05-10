package pw.binom.io.http

import kotlinx.coroutines.runBlocking
import pw.binom.*
import pw.binom.io.*
import kotlin.test.Test
import kotlin.test.assertEquals

class AsyncChunkedInputTest {

    @Test
    fun test2() {
        val output = ByteArrayOutput()
        val chunked = AsyncChunkedOutput(output.asyncOutput())

        runBlocking {
            val sb = chunked.utf8Appendable()
            sb.append("Wiki")
            chunked.flush()
            sb.append("pedia")
            sb.asyncClose()

            val out = output.data
            out.flip()

            val input = AsyncChunkedInput(out.asyncInput())
            assertEquals("Wikipedia", input.utf8Reader().readText())
        }
    }

    @Test
    fun test() {
        val output = ByteArrayOutput()
        val chunked = AsyncChunkedOutput(output.asyncOutput())

        runBlocking {
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
        val tmpBuffer = ByteBuffer.alloc(6)
        runBlocking {
            assertEquals(4, input.read(buf))
            buf.flip()
            assertEquals('W', buf.readUtf8Char(tmpBuffer))
            assertEquals('i', buf.readUtf8Char(tmpBuffer))
            assertEquals('k', buf.readUtf8Char(tmpBuffer))
            assertEquals('i', buf.readUtf8Char(tmpBuffer))
            buf.clear()
            assertEquals(5, input.read(buf))
            buf.flip()
            assertEquals('p', buf.readUtf8Char(tmpBuffer))
            assertEquals('e', buf.readUtf8Char(tmpBuffer))
            assertEquals('d', buf.readUtf8Char(tmpBuffer))
            assertEquals('i', buf.readUtf8Char(tmpBuffer))
            assertEquals('a', buf.readUtf8Char(tmpBuffer))
            buf.clear()
            assertEquals(0, input.read(buf))
        }
    }
}
