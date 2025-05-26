package pw.binom.xml.sax

interface SyncXmlVisitor2 {
  fun start() {}
  fun end() {}
  fun startConfig(name: String) {}
  fun endConfig() {}
  fun startOpenTag(tagName: String) {}
  fun endOpenTag(endTag: Boolean) {}
  fun endTag(tagName: String) {}
  fun attribute(name: String, value: String) {}
  fun comment(body: String) {}
  fun cdata(body: String) {}
  fun text(body: String) {}
}
