package pw.binom.krpc

import org.junit.Test
import pw.binom.io.asReader
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ParserTest {

    @Test
    fun test() {
val code = """
   struct User{
        string name
        int age
        bool sex
   } 
"""

        val parser = Parser(code.asReader())
        assertEquals("struct",parser.nextTokenNoSpace())
        assertEquals("User",parser.nextTokenNoSpace())
        assertEquals("{",parser.nextTokenNoSpace())
        assertEquals("string",parser.nextTokenNoSpace())
        assertEquals("name",parser.nextTokenNoSpace())
        assertEquals("int",parser.nextTokenNoSpace())
        assertEquals("age",parser.nextTokenNoSpace())
        assertEquals("bool",parser.nextTokenNoSpace())
        assertEquals("sex",parser.nextTokenNoSpace())
        assertEquals("}",parser.nextTokenNoSpace())
        assertNull(parser.nextTokenNoSpace())
    }
}