package pw.binom.xml.sax

import pw.binom.async
import pw.binom.io.asAsync
import kotlin.test.Test
import kotlin.test.assertEquals

class WriterTest {

    @Test
    fun test() {
        async {
            val sb = StringBuilder()
            val b = XmlWriterVisiter("root", sb.asAsync())
            b.start()
            b.attribute("name", "root-node")
            b.subNode("node1").apply {
                start()
                value("test-value")
                end()
            }
            b.subNode("node2").apply {
                start()
                cdata("test-value")
                end()
            }
            b.subNode("node3").apply {
                start()
                end()
            }
            b.end()
            assertEquals("<root name=\"root-node\"><node1>test-value</node1><node2><![CDATA[test-value]]></node2><node3/></root>", sb.toString())
        }
    }
}