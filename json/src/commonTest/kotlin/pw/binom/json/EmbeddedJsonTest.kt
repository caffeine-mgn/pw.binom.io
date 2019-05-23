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
            jsonNode(sb.asAsync()) {
                node("test") {
                    string("value", "key")
                }
            }

            assertEquals(sb.toString(), """{"test":{"value":"key"}}""")
        }
    }

    @Test
    fun testArray() {
        async {
            val sb = StringBuilder()
            jsonArray(sb.asAsync()) {
                node {
                    string("value", "key")
                }
            }

            assertEquals(sb.toString(), """[{"value":"key"}]""")
        }
    }
}