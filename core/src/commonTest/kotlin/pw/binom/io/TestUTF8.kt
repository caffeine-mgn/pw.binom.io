package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.decodeString
import pw.binom.encodeBytes
import pw.binom.toByteBufferUTF8
import kotlin.test.Test
import kotlin.test.assertEquals


class TestUTF8 {

    private fun testWriteChar(char: Char) {
        val s = ByteArrayOutput()
        val data = ByteBuffer.alloc(6)
        UTF8.unicodeToUtf8(char, data)
        data.flip()
        s.write(data)

        val d = s.toByteArray()
        val e = char.toString().encodeBytes()

        assertEquals(e.size, d.size)

        for (i in 0 until e.size) {
            assertEquals(e[i], d[i])
        }
    }

    private fun testReadChar(char: Char) {
        val data = char.toString().encodeBytes()
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
        assertEquals("Anton%d0%90%d0%91%20%d0%b2%d0%b3", UTF8.encode("AntonАБ вг"))
    }

    @Test
    fun testDecode() {
        assertEquals("AntonАБ вг", UTF8.decode("Anton%D0%90%D0%91%20%D0%B2%D0%B3"))
    }

    @Test
    fun testEndLine() {
        val txt = "H\r\nE"

        assertEquals(txt, txt.toByteBufferUTF8().utf8Reader().readText())

        assertEquals(
            txt,
            ByteBuffer.alloc(50).also {
                it.utf8Appendable().append(txt)
                it.flush()
                it.flip()
            }.toByteArray().decodeString()
        )
    }

    private val test_string = "donďż˘ďľ€ďľ™t"

    @Test
    fun testUnicodeToUtf8() {
        val out = ByteArrayOutput()
        out.utf8Appendable().append(test_string)
        out.data.flip()

        val buffer = ByteBuffer.alloc(100)
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
        val buffer = ByteBuffer.alloc(100)
        test_string.forEach {
            UTF8.unicodeToUtf8(it, buffer)
        }
        buffer.flip()

        val sb = StringBuilder()
        while (buffer.remaining > 0) {
            val b = buffer.get()
            sb.append(UTF8.utf8toUnicode(b, buffer))
        }

        assertEquals(test_string, sb.toString())
        buffer.close()
    }
}