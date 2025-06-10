package pw.binom.xml

import pw.binom.xml.dom.XElement

interface XmlBuilder {
  companion object {
    fun node(
      name: String,
      vararg attributes: Pair<String, String>,
      body: (XmlBuilder.() -> Unit)? = null,
    ) = node(
      name = name,
      attributes = attributes.toMap(),
      body = body
    )

    fun node(
      name: String,
      attributes: Map<String, String> = emptyMap(),
      body: (XmlBuilder.() -> Unit)? = null,
    ): XElement.Tag {
      val tag = XElement.Tag(name)
      tag.attributes += attributes
      if (body != null) {
        body(XmlBuilderImpl(tag))
      }
      return tag
    }

    fun text(value: String) = XElement.Text(value)
    fun cdata(value: String) = XElement.CDATA(value)
    fun comment(value: String) = XElement.Comment(value)
    fun config(value: String, attributes: Map<String, String> = emptyMap()): XElement.Config {
      val config = XElement.Config(value)
      config.attributes += attributes
      return config
    }
  }

  fun text(value: String)
  fun cdata(value: String)
  fun comment(value: String)
  fun config(value: String, attributes: Map<String, String> = emptyMap())
  fun node(name: String, attributes: Map<String, String> = emptyMap(), body: (XmlBuilder.() -> Unit)? = null)
}

private class XmlBuilderImpl(val root: XElement.Tag) : XmlBuilder {

  override fun text(value: String) {
    XmlBuilder.text(value).parent = root
  }

  override fun cdata(value: String) {
    XmlBuilder.cdata(value).parent = root
  }

  override fun comment(value: String) {
    XmlBuilder.comment(value).parent = root
  }

  override fun config(value: String, attributes: Map<String, String>) {
    XmlBuilder.config(value = value, attributes = attributes).parent = root
  }

  override fun node(name: String, attributes: Map<String, String>, body: (XmlBuilder.() -> Unit)?) {
    XmlBuilder.node(name = name, attributes = attributes, body = body).parent = root
  }
}
