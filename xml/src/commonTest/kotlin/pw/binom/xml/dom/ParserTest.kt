package pw.binom.xml.dom

import kotlinx.coroutines.test.runTest
import pw.binom.io.asAsync
import pw.binom.io.asReader
import pw.binom.xml.sax.XmlRootReaderVisitor
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {

  @Test
  fun test() = runTest {
    val txt =
      """<?xml version="1.0" encoding="UTF-8"?><ns1:Response ns1:name="Anton" xmlns:ns1="http://binom.pw"><value><ns1:TestData ns1:value="OLOLO"/></value></ns1:Response>"""
    val r = AsyncXmlDomReader()
    XmlRootReaderVisitor(txt.asReader().asAsync()).accept(r)
    val node = r.rootNode

    assertEquals("", node.tag)
    assertEquals(1, node.childs.size)
    assertEquals("Response", node.childs[0].tag)
    assertEquals("http://binom.pw", node.childs[0].attributes.entries.first().key.nameSpace)
    assertEquals("http://binom.pw", node.childs[0].nameSpace)
  }

  @Test
  fun test2() = runTest {
    val txt =
      """<ns1:Response ns1:name="Anton" xmlns:ns1="http://binom.pw"><value><ns1:TestData ns1:value="OLOLO"/></value></ns1:Response>"""
    val node = txt.xmlTree()!!
    val root = node

    println("->$node")
    assertEquals("Response", root.tag)
    assertEquals("http://binom.pw", root.attributes.entries.first().key.nameSpace)
    assertEquals("http://binom.pw", root.nameSpace)
  }
}
