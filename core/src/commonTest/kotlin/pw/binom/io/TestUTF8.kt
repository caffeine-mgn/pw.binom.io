package pw.binom.io

import pw.binom.asUTF8ByteArray
import pw.binom.asUTF8String
import kotlin.test.Test
import kotlin.test.assertEquals


class TestUTF8 {

    private fun testWriteChar(char: Char) {
        val s = ByteArrayOutputStream()
        UTF8.write(char, s)

        val d = s.toByteArray()
        val e = char.toString().asUTF8ByteArray()

        assertEquals(e.size, d.size)

        for (i in 0 until e.size) {
            assertEquals(e[i], d[i])
        }
    }

    private fun testReadChar(char: Char) {
        val data = char.toString().asUTF8ByteArray()
        assertEquals(char, UTF8.read(ByteArrayInputStream(data)))
    }

    @Test
    fun testReadENG() {
        ('a'..'z').forEach {
            testReadChar(it)
        }
        ('A'..'Z').forEach {
            testReadChar(it)
        }
    }

    @Test
    fun testReadRUS() {
        ('а'..'я').forEach {
            testReadChar(it)
        }
        ('А'..'Я').forEach {
            testReadChar(it)
        }
    }


    @Test
    fun testWriteENG() {
        ('a'..'z').forEach {
            testWriteChar(it)
        }
        ('A'..'Z').forEach {
            testWriteChar(it)
        }
    }

    @Test
    fun testWriteRUS() {
        ('а'..'я').forEach {
            testWriteChar(it)
        }

        ('А'..'Я').forEach {
            testWriteChar(it)
        }

        testWriteChar('ё')
        testWriteChar('Ё')
    }

    @Test
    fun testEncode() {
        assertEquals("Anton%d0%90%d0%91%20%d0%b2%d0%b3", UTF8.urlEncode("AntonАБ вг"))
    }

    @Test
    fun testDecode() {
        assertEquals("AntonАБ вг", UTF8.urlDecode("Anton%D0%90%D0%91%20%D0%B2%D0%B3"))
    }

    @Test
    fun testEndLine() {
        val txt = "Hello\r\nFrom Server"

        assertEquals(txt, ByteArrayInputStream(txt.asUTF8ByteArray()).utf8Reader().readText())

        assertEquals(txt,
                ByteArrayOutputStream().also {
                    it.utf8Appendable().append(txt)
                    it.flush()
                }.toByteArray().asUTF8String()
        )
    }
}