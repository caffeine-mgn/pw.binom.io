package pw.binom.xml.sax

import pw.binom.io.AsyncAppendable

class AsyncXmlWriterVisitor(
  nodeName: String,
  val appendable: AsyncAppendable,
) : AsyncXmlVisitor, AbstractXmlWriterVisitor(nodeName = nodeName) {

  override suspend fun start() {
    commonStart { appendable.append(it) }
  }

  override suspend fun comment(body: String) {
    commonComment(body = body) { appendable.append(it) }
  }

  override suspend fun end() {
    commonEnd { appendable.append(it) }
  }

  override suspend fun attributeName(name: String) {
    commonAttributeName(name) { appendable.append(it) }
    super.attributeName(name)
  }

  override suspend fun attributeValue(value: String?) {
    commonAttributeValue(value) { appendable.append(it) }
    super.attributeValue(value)
  }

  override suspend fun value(body: String) {
    commonValue(
      body = body,
      appendString = { appendable.append(it) },
      appendChar = { appendable.append(it) },
    )
  }

  override suspend fun subNode(name: String): AsyncXmlVisitor {
    commonSubNode { appendable.append(it) }
    return AsyncXmlWriterVisitor(name, appendable)
  }

  override suspend fun cdata(body: String) {
    commonCdata(body) { appendable.append(it) }
  }
}
