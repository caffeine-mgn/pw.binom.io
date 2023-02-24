package pw.binom.io

import pw.binom.toByteBufferUTF8
import pw.binom.url.UrlEncoder
import kotlin.test.Test
import kotlin.test.assertEquals

class TestUTF8 {

    private fun testWriteChar(char: Char) {
        val s = ByteArrayOutput()
        val data = ByteBuffer(6)
        UTF8.unicodeToUtf8(char, data)
        data.flip()
        s.write(data)

        val d = s.toByteArray()
        val e = char.toString().encodeToByteArray()

        assertEquals(e.size, d.size)

        for (i in 0 until e.size) {
            assertEquals(e[i], d[i])
        }
    }

    private fun testReadChar(char: Char) {
        val data = char.toString().encodeToByteArray()
        assertEquals(char, UTF8.utf8toUnicode(data[0], data, 1))
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
        testWriteChar('@')
        testWriteChar('*')
        testWriteChar('/')
        testWriteChar('\\')
        testWriteChar('\n')
        testWriteChar('\t')
        testWriteChar('?')

        testReadChar('ё')
        testReadChar('Ё')
        testReadChar('@')
        testReadChar('*')
        testReadChar('/')
        testReadChar('\\')
        testReadChar('\n')
        testReadChar('\t')
        testReadChar('?')
    }

    @Test
    fun testEncode() {
        assertEquals("Anton%D0%90%D0%91%20%D0%B2%D0%B3", UrlEncoder.encode("AntonАБ вг"))
    }

    @Test
    fun testDecode() {
        assertEquals("AntonАБ вг", UrlEncoder.decode("Anton%D0%90%D0%91%20%D0%B2%D0%B3"))
    }

    @Test
    fun testEndLine() {
        val txt = "H\r\nE"
        println("#1")
        assertEquals(txt, txt.toByteBufferUTF8().utf8Reader().readText())
        println("#2")
        assertEquals(
            txt,
            ByteBuffer(50).also {
                println("#2-1")
                it.utf8Appendable().append(txt)
                println("#2-2")
                it.flush()
                println("#2-3")
                it.flip()
                println("#2-4")
            }.toByteArray().decodeToString(),
        )
        println("#3")
    }

    private val test_string = "donďż˘ďľ€ďľ™t"

    @Test
    fun testUnicodeToUtf8() {
        val out = ByteArrayOutput()
        out.utf8Appendable().append(test_string)
        out.data.flip()

        val buffer = ByteBuffer(100)
        test_string.forEach {
            UTF8.unicodeToUtf8(it, buffer)
        }
        buffer.flip()

        val bytes = "64 6f 6e c4 8f c5 bc cb 98 c4 8f c4 be e2 82 ac c4 8f c4 be e2 84 a2 74"
            .split(' ')
            .map { it.toUByte(16).toByte() }

        assertEquals(bytes.size, buffer.remaining)
        buffer.close()
    }

    @Test
    fun testdd() {
        val buffer = ByteBuffer(100)
        test_string.forEach {
            UTF8.unicodeToUtf8(it, buffer)
        }
        buffer.flip()

        val sb = StringBuilder()
        while (buffer.remaining > 0) {
            val b = buffer.getByte()
            sb.append(UTF8.utf8toUnicode(b, buffer))
        }

        assertEquals(test_string, sb.toString())
        buffer.close()
    }
}
