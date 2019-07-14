package pw.binom.json

import pw.binom.async
import pw.binom.io.asAsync
import kotlin.test.Test
import kotlin.test.assertEquals

class WriterTest {

    @Test
    fun `empty array`() {
        async {
            val sb = StringBuilder()

            val w = JsonWriter(sb.asAsync())

            w.arrayValue().also {
                it.start()
                it.end()
            }

            assertEquals("[]", sb.toString())
        }
    }

    @Test
    fun `empty object`() {
        async {
            val sb = StringBuilder()

            val w = JsonWriter(sb.asAsync())

            w.objectValue().also {
                it.start()
                it.end()
            }

            assertEquals("{}", sb.toString())
        }
    }

    @Test
    fun `spec symbols`() {
        async {
            val sb = StringBuilder()

            val w = JsonWriter(sb.asAsync())

            w.objectValue().also {
                it.start()
                it.property("name").textValue("\\Hello\nFrom\n\"Hollywood\"")
                it.end()
            }

            assertEquals("""{"name":"\\Hello\nFrom\n\"Hollywood\""}""", sb.toString())
        }
    }
}