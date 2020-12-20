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
    fun substringTest2() {
        val out = ByteArrayOutput()
        val outasync = out
        val pool = ByteBufferPool(10)
        val appender = outasync.bufferedWriter(pool)
        val d = mapOf(
            "user" to "postgres",
            "database" to "test",
            "client_encoding" to "utf-8",
            "DateStyle" to "ISO",
        )
            d.forEach {
                appender.append(it.key)
                appender.flush()
                appender.append(it.value)
                appender.flush()
            }
//        val txt = "HelloWorld"
//        assertEquals(txt.substring(1, 9), txt.toCharArray().toCharBuffer().subString(1, 9))
    }
}