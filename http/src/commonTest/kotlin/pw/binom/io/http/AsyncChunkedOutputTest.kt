package pw.binom.io.http

import pw.binom.*
import pw.binom.io.ByteArrayOutput
import pw.binom.io.utf8Appendable
import kotlin.test.Test
import kotlin.test.assertEquals

fun ByteBuffer.print() {
    val p = position
    val l = limit
    val buf = ByteBuffer.alloc(4)
    try {
        while (remaining > 0) {
            val c = readUtf8Char(buf)!!
            when (c) {
                '\r' -> print("\\r")
                '\n' -> println("\\n")
                else -> print(c)
            }
        }
    } finally {
        position = p
        limit = l
    }
}

class AsyncChunkedOutputTest {

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
        val buf = ByteBuffer.alloc(4)
        assertEquals('4', out.readUtf8Char(buf))
        assertEquals('\r', out.readUtf8Char(buf))
        assertEquals('\n', out.readUtf8Char(buf))
        assertEquals('W', out.readUtf8Char(buf))
        assertEquals('i', out.readUtf8Char(buf))
        assertEquals('k', out.readUtf8Char(buf))
        assertEquals('i', out.readUtf8Char(buf))
        assertEquals('\r', out.readUtf8Char(buf))
        assertEquals('\n', out.readUtf8Char(buf))
        assertEquals('5', out.readUtf8Char(buf))
        assertEquals('\r', out.readUtf8Char(buf))
        assertEquals('\n', out.readUtf8Char(buf))
        assertEquals('p', out.readUtf8Char(buf))
        assertEquals('e', out.readUtf8Char(buf))
        assertEquals('d', out.readUtf8Char(buf))
        assertEquals('i', out.readUtf8Char(buf))
        assertEquals('a', out.readUtf8Char(buf))
        assertEquals('\r', out.readUtf8Char(buf))
        assertEquals('\n', out.readUtf8Char(buf))
        assertEquals('0', out.readUtf8Char(buf))
        assertEquals('\r', out.readUtf8Char(buf))
        assertEquals('\n', out.readUtf8Char(buf))
        assertEquals('\r', out.readUtf8Char(buf))
        assertEquals('\n', out.readUtf8Char(buf))
        assertEquals(0, out.remaining)
        output.close()
    }
}