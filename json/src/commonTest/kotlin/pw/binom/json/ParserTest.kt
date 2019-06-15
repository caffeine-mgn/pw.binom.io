package pw.binom.json

import pw.binom.async
import pw.binom.io.asAsync
import pw.binom.io.asReader
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {

    @Test
    fun test() {
        async {
            val txt = """{ "test" : "Hello!" , "array":["OLOLO"],"array1":[],"bool":false,"null":null,"int":123,"float1":123.5,"float2":0.5}"""
            val r = txt.asReader().asAsync()

            val sb = StringBuilder()

            JsonReader(r).accept(JsonWriter(sb.asAsync()))

            println("Result: $sb")
            assertEquals("""{"test":"Hello!","array":["OLOLO"],"array1":[],"bool":false,"null":null,"int":123,"float1":123.5,"float2":0.5}""", sb.toString())
        }
    }

    @Test
    fun `spec symbols`() {
        val txt = """{"name":"Hello\nFrom\n\"Hollywood\""}"""

        async {
            val r = JsonDomReader()
            JsonReader(txt.asReader().asAsync()).accept(r)
            assertEquals("Hello\nFrom\n\"Hollywood\"",r.node.obj["name"]!!.text)
        }
    }
}