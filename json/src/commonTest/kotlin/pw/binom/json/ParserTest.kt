package pw.binom.json

import pw.binom.io.asReader
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {

    @Test
    fun test() {
        val txt = """{ "test" : "Hello!" , "array":["OLOLO"],"array1":[],"bool":false,"null":null,"int":123,"float1":123.5,"float2":.5}"""
        val r = txt.asReader()

        val sb = StringBuilder()

        JsonReader(r).accept(JsonWriter(sb))

        println("Result: $sb")
        assertEquals("""{"test":"Hello!","array":["OLOLO"],"array1":[],"bool":false,"null":null,"int":123.0,"float1":123.5,"float2":0.5}""", sb.toString())
    }
}