package pw.binom.xml.sax

import pw.binom.io.AsyncAppendable

class AsyncXmlRootWriterVisitor private constructor(val appendable: AsyncAppendable, val charset: String?) :
  AsyncXmlVisitor {
  companion object {
    fun withHeader(appendable: AsyncAppendable, charset: String = "UTF-8") = AsyncXmlRootWriterVisitor(
      appendable = appendable,
      charset = charset,
    )

    fun withoutHeader(appendable: AsyncAppendable) = AsyncXmlRootWriterVisitor(
      appendable = appendable,
      charset = null,
    )
  }

  private var started = false
  private var endded = false
  override suspend fun start() {
    if (started) {
      throw IllegalStateException("Root Node already started")
    }
    started = true
    if (charset != null) {
      appendable.append("<?xml version=\"1.0\" encoding=\"$charset\"?>")
    }
  }

  override suspend fun comment(body: String) {
    appendable.append("<!--").append(body).append("-->")
  }

  override suspend fun end() {
    if (!started) {
      throw IllegalStateException("Root Node not started")
    }
    if (endded) {
      throw IllegalStateException("Root Node already closed")
    }
    endded = true
  }

  override suspend fun attribute(name: String, value: String?) {
    throw IllegalStateException("Root node not supports attributes")
  }

  override suspend fun value(body: String) {
    if (body.isBlank()) {
      return
    }
    throw IllegalStateException("Root node not supports attributes")
  }

  override suspend fun cdata(body: String) {
    throw IllegalStateException("Root node not supports attributes")
  }

  override suspend fun subNode(name: String): AsyncXmlVisitor {
    if (!started) {
      throw IllegalStateException("Root Node not started")
    }
    if (endded) {
      throw IllegalStateException("Root Node already closed")
    }
    return AsyncXmlWriterVisitor(name, appendable)
  }
}
