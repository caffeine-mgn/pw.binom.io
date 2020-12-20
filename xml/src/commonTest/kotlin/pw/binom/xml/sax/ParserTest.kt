package pw.binom.xml.sax

import pw.binom.async
import pw.binom.io.asAsync
import pw.binom.io.asReader
import pw.binom.xml.internal.XmlReaderVisiter
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {

    @Test
    fun test31() {
        async {
            val txt = """<?xml version="1.0" encoding="UTF-8"?>
<r:dd name="KDE" title="DE"><name>df</name><b><![CDATA[TEST Hi!]]></b><test fff="sdf"/></r:dd>"""

            val r = XmlRootReaderVisitor(txt.asReader().asAsync())

            val sb = StringBuilder()
            val w = XmlRootWriterVisitor(sb.asAsync())
            try {
                w.start()
                r.accept(w)
                w.end()
            } catch (e: Throwable) {
                println("ERROR: $e\nXML=$sb")
                e.printStackTrace()
                throw e
            }
            println("==>$sb")
        }
    }

    @Test
    fun test() {
        async {
            val txt = "<r><bbb a=\"b\"></bbb><c>123456</c><t/></r>"

            val r = XmlReaderVisiter(txt.asReader().asAsync())

            val sb = StringBuilder()
            val w = XmlRootWriterVisitor(sb.asAsync())
            try {
                w.start()
                r.accept(w)
                w.end()
            } catch (e: Throwable) {
                println("ERROR: $e\nXML=$sb")
                throw e
            }
            println("==>$sb")
        }
    }

    @Test
    fun test2() {
        val txt = """<?xml version="1.0" encoding="UTF-8"?><root title="Binom"></root>"""
        async {
            val sb = StringBuilder()
            val root = XmlRootWriterVisitor(sb.asAsync())
            root.start()
            XmlRootReaderVisitor(txt.asReader().asAsync()).accept(root)
            root.end()
            assertEquals("""<?xml version="1.0" encoding="UTF-8"?><root title="Binom"/>""", sb.toString())
        }
    }

    @Test
    fun test3() {
        async {
            try {
                val txt = """<?xml version="1.0" encoding="utf-8" ?>
                |<D:propfind xmlns:D="DAV:" xmlns:L="LCGDM:">
                |   <D:prop>
                |       <D:displayname/>
                |       <D:getlastmodified/>
                |       <D:creationdate/>
                |       <D:getcontentlength/>
                |       <D:quota-used-bytes/>
                |       <D:resourcetype>
                |           <D:collection/>
                |       </D:resourcetype>
                |       <L:mode/>
                |       <D:owner></D:owner>
                |       <D:group></D:group>
                |   </D:prop>
                |</D:propfind>""".trimMargin()
                val sb = StringBuilder()
                val root = XmlRootWriterVisitor(sb.asAsync())
                root.start()
                XmlRootReaderVisitor(txt.asReader().asAsync()).accept(root)
                root.end()
                println(sb)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}