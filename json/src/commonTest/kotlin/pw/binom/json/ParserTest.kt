package pw.binom.json

import pw.binom.async
import pw.binom.io.asAsync
import pw.binom.io.asReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        val txt = """{"name":"\\Hello\nFrom\n\"Hollywood\""}"""

        async {
            val r = JsonDomReader()
            JsonReader(txt.asReader().asAsync()).accept(r)
            assertEquals("\\Hello\nFrom\n\"Hollywood\"",r.node.obj["name"]!!.string)
        }
    }

    @Test
    fun test2(){
        val txt="""{"result":[{"path":"234234","isTask":true,"@type":"pw.binom.builder.remote.TaskItem","name":"234234"},{"path":"Dir 3","isTask":false,"@type":"pw.binom.builder.remote.TaskItem","name":"Dir 3"},{"path":"Dir2","isTask":false,"@type":"pw.binom.builder.remote.TaskItem","name":"Dir2"},{"path":"sleep","isTask":true,"@type":"pw.binom.builder.remote.TaskItem","name":"sleep"}],"error":false}"""
        async {
            val node = txt.parseJSON()
            assertTrue(node is JsonObject)
        }
    }
}