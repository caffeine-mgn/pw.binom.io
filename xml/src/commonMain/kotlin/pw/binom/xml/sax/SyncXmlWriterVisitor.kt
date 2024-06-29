package pw.binom.xml.sax

class SyncXmlWriterVisitor(
  nodeName: String,
  val appendable: Appendable,
) : SyncXmlVisitor, AbstractXmlWriterVisitor(nodeName = nodeName) {

  override fun start(tagName: String) {
    commonStart { appendable.append(it) }
  }

  override fun comment(body: String) {
    commonComment(body = body) { appendable.append(it) }
  }

  override fun end() {
    commonEnd { appendable.append(it) }
  }

  override fun attributeName(name: String) {
    commonAttributeName(name) { appendable.append(it) }
    super.attributeName(name)
  }

  override fun attributeValue(value: String?) {
    commonAttributeValue(value) { appendable.append(it) }
    super.attributeValue(value)
  }

  override fun value(body: String) {
    commonValue(
      body = body,
      appendString = { appendable.append(it) },
      appendChar = { appendable.append(it) },
    )
  }

  override fun subNode(name: String): SyncXmlVisitor {
    commonSubNode { appendable.append(it) }
    return SyncXmlWriterVisitor(name, appendable)
  }

  override fun cdata(body: String) {
    commonCdata(body) { appendable.append(it) }
  }
}
