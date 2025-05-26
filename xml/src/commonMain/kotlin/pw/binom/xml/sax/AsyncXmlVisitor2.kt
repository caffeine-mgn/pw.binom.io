package pw.binom.xml.sax

interface AsyncXmlVisitor2 {
  suspend fun start() {}
  suspend fun end() {}
  suspend fun startConfig(name: String) {}
  suspend fun endConfig() {}
  suspend fun startOpenTag(tagName: String) {}
  suspend fun endOpenTag(endTag: Boolean) {}
  suspend fun endTag(tagName: String) {}
  suspend fun attribute(name: String, value: String) {}
  suspend fun comment(body: String) {}
  suspend fun cdata(body: String) {}
  suspend fun text(body: String) {}
}
