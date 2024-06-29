package pw.binom.xml.sax

sealed class AbstractXmlWriterVisitor(
  val nodeName: String,
) {

  companion object {
    @PublishedApi
    internal val START = 1

    @PublishedApi
    internal val BODY = 2

    @PublishedApi
    internal val END = 3
  }

  protected var progress = 0
  protected var subnode = 0
  protected var bodyStart = false
  protected var started = false
  protected var endded = false

  init {
    if (XmlConsts.TAG_START in nodeName || XmlConsts.TAG_END in nodeName) {
      throw IllegalArgumentException("Invalid node name \"$nodeName\"")
    }
  }

  protected inline fun commonAttributeValue(value: String?, append: (String) -> Unit) {
    if (value != null) {
      append("=\"")
      append(value)
      append("\"")
    }
  }

  protected inline fun commonValue(body: String, appendString: (String) -> Unit, appendChar: (Char) -> Unit) {
    if (progress < START) {
      throwNodeNotStarted()
    }
    if (progress >= END) {
      throwNodeAlreadyClosed()
    }
    if (progress == START) {
      progress = BODY
      appendChar(XmlConsts.TAG_END)
    }
    body.forEach {
      when (it) {
        XmlConsts.TAG_START -> appendString("&lt;")
        XmlConsts.TAG_END -> appendString("&gt;")
        '&' -> appendString("&#38;") //&amp;
        '\'' -> appendString("&#39;") //&apos;
        '"' -> appendString("&#34;") //&quot;
        else -> appendChar(it)
      }
    }
  }

  protected inline fun commonSubNode(append: (String) -> Unit) {
    if (progress < START) {
      throwNodeNotStarted()
    }
    if (progress >= END) {
      throwNodeAlreadyClosed()
    }
    if (progress == START) {
      progress = BODY
      append(XmlConsts.TAG_END.toString())
    }
  }

  protected inline fun commonCdata(body: String, append: (String) -> Unit) {
    if (progress < START) {
      throwNodeNotStarted()
    }
    if (progress >= END) {
      throwNodeAlreadyClosed()
    }
    if (progress == START) {
      progress = BODY
      append(XmlConsts.TAG_END.toString())
    }
    append("<![CDATA[")
    append(body)
    append("]]>")
  }

  protected inline fun commonAttributeName(name: String, append: (String) -> Unit) {
    if (progress < START) {
      throwNodeNotStarted()
    }

    if (progress > START) {
      throw IllegalStateException("Can't write attribute after body")
    }
    append(" ")
    append(name)
  }

  protected inline fun commonComment(body: String, append: (String) -> Unit) {
    if (progress == START) {
      progress = BODY
      append(XmlConsts.TAG_END.toString())
    }
    append("<!--")
    append(body)
    append("-->")
  }

  protected inline fun commonStart(append: (String) -> Unit) {
    if (progress >= START) {
      throw IllegalStateException("Node already started")
    }
    append(XmlConsts.TAG_START.toString())
    append(nodeName)
    started = true
    progress = START
  }

  protected inline fun commonEnd(append: (String) -> Unit) {
    if (progress >= END) {
      throw IllegalStateException("Node \"$nodeName\" already ended")
    }
    endded = true
    when (progress) {
      START -> append("/>")
      BODY -> {
        append("</")
        append(nodeName)
        append(XmlConsts.TAG_END.toString())
      }

      else -> throw IllegalStateException()
    }
    progress = END
  }

  protected fun throwNodeAlreadyClosed(): Nothing = throw IllegalStateException("Node \"$nodeName\" already closed")
  protected fun throwNodeNotStarted(): Nothing = throw IllegalStateException("Node \"$nodeName\" not started")
}
