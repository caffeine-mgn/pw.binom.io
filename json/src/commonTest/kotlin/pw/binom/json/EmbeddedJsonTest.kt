package pw.binom.json

import pw.binom.async
import pw.binom.io.asAsync
import kotlin.test.Test
import kotlin.test.assertEquals

class EmbeddedJsonTest {

    @Test
    fun testNode() {
        async {
            val sb = StringBuilder()
            jsonNode {
                node("test") {
                    string("value", "key")
                }
            }.write(sb.asAsync())

            assertEquals(sb.toString(), """{"test":{"value":"key"}}""")
        }
    }

    @Test
    fun testArray() {
        async {
            val sb = StringBuilder()
            jsonArray {
                node {
                    string("value", "key")
                }
            }.write(sb.asAsync())

            assertEquals(sb.toString(), """[{"value":"key"}]""")
        }
    }
}