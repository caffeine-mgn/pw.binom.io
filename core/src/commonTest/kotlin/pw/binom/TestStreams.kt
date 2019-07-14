package pw.binom

import pw.binom.io.ByteArrayInputStream
import pw.binom.io.ByteArrayOutputStream
import pw.binom.io.readln
import pw.binom.io.writeln
import kotlin.test.Test
import kotlin.test.assertEquals

class TestStreams {

    @Test
    fun testWriteLn() {
        val txt1 = "Hello"
        val txt2 = "World"
        val str = ByteArrayOutputStream().apply {
            writeln(txt1)
            writeln(txt2)
        }.toByteArray().asUTF8String()

        assertEquals("$txt1\r\n$txt2\r\n", str)
    }

    @Test
    fun testReadLn() {
        val txt1 = "Hello"
        val txt2 = "World"
        ByteArrayOutputStream().apply {
            writeln(txt1)
            writeln(txt2)
        }.toByteArray().let { ByteArrayInputStream(it) }.apply {
            assertEquals(txt1, readln())
            assertEquals(txt2, readln())
        }
    }
}