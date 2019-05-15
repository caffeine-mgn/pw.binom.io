package pw.binom.json

import kotlin.test.Test
import kotlin.test.assertEquals

class WriterTest {

    @Test
    fun `empty array`() {
        val sb = StringBuilder()

        val w = JsonWriter(sb)

        w.arrayValue().also {
            it.start()
            it.end()
        }

        assertEquals("[]", sb.toString())
    }

    @Test
    fun `empty object`() {
        val sb = StringBuilder()

        val w = JsonWriter(sb)

        w.objectValue().also {
            it.start()
            it.end()
        }

        assertEquals("{}", sb.toString())
    }
}