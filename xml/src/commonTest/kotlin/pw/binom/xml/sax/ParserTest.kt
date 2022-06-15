package pw.binom.xml.sax

import kotlinx.coroutines.test.runTest
import pw.binom.io.StringReader
import pw.binom.io.asAsync
import pw.binom.io.asReader
import pw.binom.xml.dom.xmlTree
import kotlin.test.Test
import kotlin.test.assertEquals

private const val XML_START = """<?xml version="1.0" encoding="UTF-8"?>"""

class ParserTest {

    @Test
    fun test31() = runTest {
        val txt = """$XML_START
<r:dd name="KDE" title="DE"><name>df</name><b><![CDATA[TEST Hi!]]></b><test fff="sdf"/></r:dd>"""

        val r = XmlRootReaderVisitor(txt.asReader().asAsync())

        val sb = StringBuilder()
        val w = AsyncXmlRootWriterVisitor(sb.asAsync())
        w.start()
        r.accept(w)
        w.end()
    }

    @Test
    fun testMinus() = runTest {
        val txt = """$XML_START
            |<data><title-name attr="value"/> <title2-name attr="value"></title2-name> </data>
        """.trimMargin()

        val r = XmlRootReaderVisitor(txt.asReader().asAsync())
        val tree = txt.asReader().asAsync().xmlTree()
        assertEquals("data", tree.tag)
        assertEquals(2, tree.childs.size)
        assertEquals("title-name", tree.childs[0].tag)
        assertEquals("title2-name", tree.childs[1].tag)
        println("tree: $tree")
    }

    @Test
    fun testParseComment() = runTest {
        val txt = """<r><bbb a="b"><!--Привет - мир!--></bbb><c>123456</c><t/></r>"""

        val r = AsyncXmlReaderVisitor(txt.asReader().asAsync())

        val sb = StringBuilder()
        val w = AsyncXmlRootWriterVisitor(sb.asAsync())
        w.start()
        r.accept(w)
        w.end()
        println("->$sb")
        assertEquals("$XML_START$txt", sb.toString())
    }

    @Test
    fun test() = runTest {
        val txt = "<r><bbb a=\"b\"></bbb><c>123456</c><t/></r>"

        val r = AsyncXmlReaderVisitor(txt.asReader().asAsync())

        val sb = StringBuilder()
        val w = AsyncXmlRootWriterVisitor(sb.asAsync())
        w.start()
        r.accept(w)
        w.end()
    }

    @Test
    fun tagBodyTest() = runTest {
//        val txt = """<?xml version="1.0" encoding="UTF-8"?><root><![CDATA[AA BB CC]]></root>"""
        val txt = """<?xml version="1.0" encoding="UTF-8"?><root>AA BB CC</root>"""
        val sb = StringBuilder()
        val root = AsyncXmlRootWriterVisitor(sb.asAsync())
        root.start()
        XmlRootReaderVisitor(txt.asReader().asAsync()).accept(root)
        root.end()
        assertEquals("""<?xml version="1.0" encoding="UTF-8"?><root>AA BB CC</root>""", sb.toString())
    }

    @Test
    fun test4() = runTest {
        val txt = """
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

        StringReader(txt).asAsync().xmlTree()
        println("All is ok")
    }

    @Test
    fun test2() = runTest {
        val txt = """<?xml version="1.0" encoding="UTF-8"?><root title="Binom"></root>"""
        val sb = StringBuilder()
        val root = AsyncXmlRootWriterVisitor(sb.asAsync())
        root.start()
        XmlRootReaderVisitor(txt.asReader().asAsync()).accept(root)
        root.end()
        assertEquals("""<?xml version="1.0" encoding="UTF-8"?><root title="Binom"/>""", sb.toString())
    }

    @Test
    fun test3() = runTest {
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
                |</D:propfind>
        """.trimMargin()
        val sb = StringBuilder()
        val root = AsyncXmlRootWriterVisitor(sb.asAsync())
        root.start()
        XmlRootReaderVisitor(txt.asReader().asAsync()).accept(root)
        root.end()
        println(sb)
    }
}
