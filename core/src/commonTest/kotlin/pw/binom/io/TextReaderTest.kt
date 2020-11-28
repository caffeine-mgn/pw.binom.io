package pw.binom.io

import pw.binom.ByteBuffer
import pw.binom.test_data_hello_bytes_windows_1251
import pw.binom.wrap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

//class TextReaderTest {
//
//    @Test
//    fun readByCharTest() {
//        val input = ByteBuffer.wrap(test_data_hello_bytes_windows_1251)
//        val reader = TextReader("windows-1251", input)
//        assertEquals('П', assertNotNull(reader.read()))
//        assertEquals('р', assertNotNull(reader.read()))
//        assertEquals('и', assertNotNull(reader.read()))
//        assertEquals('в', assertNotNull(reader.read()))
//        assertEquals('е', assertNotNull(reader.read()))
//        assertEquals('т', assertNotNull(reader.read()))
//        assertNull(reader.read())
//    }
//
//    @Test
//    fun readByCharArrayTest() {
//        val input = ByteBuffer.wrap(test_data_hello_bytes_windows_1251)
//        val reader = TextReader("windows-1251", input)
//        val d = CharArray(10)
//        assertEquals(6, reader.read(d))
//        assertEquals('П', assertNotNull(d[0]))
//        assertEquals('р', assertNotNull(d[1]))
//        assertEquals('и', assertNotNull(d[2]))
//        assertEquals('в', assertNotNull(d[3]))
//        assertEquals('е', assertNotNull(d[4]))
//        assertEquals('т', assertNotNull(d[5]))
//        assertNull(reader.read())
//    }
//}