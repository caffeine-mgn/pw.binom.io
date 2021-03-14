package pw.binom

import pw.binom.io.ByteArrayOutput
import pw.binom.io.bufferedWriter
import kotlin.test.Test
import kotlin.test.assertEquals

class CharBufferTest {

    @Test
    fun substringTest() {
        val txt = "HelloWorld"
        assertEquals(txt.substring(1, 9), txt.toCharArray().toCharBuffer().subString(1, 9))
    }

    @Test
    fun realloc() {
        val b = CharBuffer.alloc(30)
        b.position = 10
        val n = b.realloc(50)
        assertEquals(10, n.position)
        assertEquals(30, n.limit)
    }


    @Test
    fun substringTest2() {
        val v = 'Я'.toInt()
        val v0 = v shr 8
        val v1 = v and 0xff
        println("0->${v0}, 1->${v1}")
        println("size: ${Char.SIZE_BYTES}")
        val out = ByteArrayOutput()
        val outasync = out.asyncOutput()
        val pool = ByteBufferPool(10)
        val appender = outasync.bufferedWriter(pool)
        val d = mapOf(
            "Привет🠈user" to "postgres",
//            "user" to "postgres",
            "database" to "test",
            "client_encoding" to "utf-8",
            "DateStyle" to "ISO",
        )
        println("#0")
        async {
            d.forEach {
                appender.append(it.key)
                appender.flush()
                appender.append(it.value)
                appender.flush()
            }
        }
        assertEquals(72, out.size)
        out.trimToSize()
        out.data.flip()
        assertEquals(
            "Привет\uD83E\uDC08userpostgresdatabasetestclient_encodingutf-8DateStyleISO",
            out.data.toByteArray().decodeToString()
        )
//        val txt = "HelloWorld"
//        assertEquals(txt.substring(1, 9), txt.toCharArray().toCharBuffer().subString(1, 9))
    }
}