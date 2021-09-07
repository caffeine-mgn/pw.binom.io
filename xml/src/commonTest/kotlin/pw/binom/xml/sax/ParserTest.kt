package pw.binom.xml.sax

import pw.binom.async
import pw.binom.async2
import pw.binom.getOrException
import pw.binom.io.StringReader
import pw.binom.io.asAsync
import pw.binom.io.asReader
import pw.binom.xml.dom.xmlTree
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
            val w = AsyncXmlRootWriterVisitor(sb.asAsync())
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

            val r = AsyncXmlReaderVisitor(txt.asReader().asAsync())

            val sb = StringBuilder()
            val w = AsyncXmlRootWriterVisitor(sb.asAsync())
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
    fun tagBodyTest() {
//        val txt = """<?xml version="1.0" encoding="UTF-8"?><root><![CDATA[AA BB CC]]></root>"""
        val txt = """<?xml version="1.0" encoding="UTF-8"?><root>AA BB CC</root>"""
        async {
            val sb = StringBuilder()
            val root = AsyncXmlRootWriterVisitor(sb.asAsync())
            root.start()
            XmlRootReaderVisitor(txt.asReader().asAsync()).accept(root)
            root.end()
            assertEquals("""<?xml version="1.0" encoding="UTF-8"?><root>AA BB CC</root>""", sb.toString())
        }
    }

    @Test
    fun test4(){
        val txt="""
<?xml version="1.0" encoding="utf-8" ?>
<D:multistatus xmlns:D="DAV:">
<D:response>
<D:href>/24e1519c-6846-4f0a-99c6-55c296168fc0</D:href>
<D:propstat>
<D:prop>
<D:displayname>24e1519c-6846-4f0a-99c6-55c296168fc0</D:displayname>
<D:getcontentlength>1725852</D:getcontentlength>
<D:getlastmodified>Sun, 05 Sep 2021 01:48:50 GMT</D:getlastmodified>
<D:resourcetype></D:resourcetype>
<D:lockdiscovery/>
<D:supportedlock>
</D:supportedlock>
</D:prop>
<D:status>HTTP/1.1 200 OK</D:status>
</D:propstat>
</D:response>
</D:multistatus>
        """

        async2 {
            StringReader(txt).asAsync().xmlTree()
            println("All is ok")
        }.getOrException()
    }

    @Test
    fun test2() {
        val txt = """<?xml version="1.0" encoding="UTF-8"?><root title="Binom"></root>"""
        async {
            val sb = StringBuilder()
            val root = AsyncXmlRootWriterVisitor(sb.asAsync())
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
                val root = AsyncXmlRootWriterVisitor(sb.asAsync())
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