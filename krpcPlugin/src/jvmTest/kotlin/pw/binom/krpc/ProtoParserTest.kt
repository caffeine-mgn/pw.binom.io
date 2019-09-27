package pw.binom.krpc

import org.junit.Test
import pw.binom.io.asReader
import kotlin.test.assertEquals

class ProtoParserTest {

    @Test
    fun test() {
        val code = """
            package my.  test
   struct User {
        string name
        array<string > age
        map<string, User> sex
   } 
"""

        val result = parseProto(code.asReader())
        assertEquals("my.test", result.packageName)
        assertEquals(1, result.structs.size)
        val struct = result.structs[0]
        assertEquals("User", struct.name)
        assertEquals(3, struct.fields.size)
        assertEquals("name", struct.fields[0].name)

        assertEquals("age", struct.fields[1].name)

        assertEquals("sex", struct.fields[2].name)
    }
}