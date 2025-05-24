package pw.binom.xml

import pw.binom.io.Reader
import pw.binom.io.StringReader
import pw.binom.xml.dom.XElement
import pw.binom.xml.sax.XmlVisitor

object XmlParser {
  fun parse(xml: Reader): List<XElement> {
    val visitor = XmlXElementDomVisitor()
    parse(
      tokenizer = XmlTokenizer(xml),
      visitor = visitor
    )
    return visitor.elements
  }

  fun parse(xml: String) =
    parse(StringReader(xml))

  fun parse(tokenizer: XmlTokenizer, visitor: XmlVisitor) {
    visitor.start()
    while (tokenizer.next()) {
      parseElement(xmlTokenizer = tokenizer, visitor = visitor)
    }
    visitor.end()
  }

  private fun trimCdata(text: String) =
    text.removePrefix("<![CDATA[").removeSuffix("]]>")

  private fun parseElement(xmlTokenizer: XmlTokenizer, visitor: XmlVisitor) {
    when (xmlTokenizer.type) {
      TokenType2.TAG_START -> readTag(xmlTokenizer = xmlTokenizer, visitor = visitor)
      TokenType2.TAG_END -> TODO()
      TokenType2.SYMBOL -> visitor.text(xmlTokenizer.text)
      TokenType2.WHITESPACE -> visitor.text(xmlTokenizer.text)
      TokenType2.STRING -> visitor.text(xmlTokenizer.text)
      TokenType2.TEXT -> visitor.text(xmlTokenizer.text)
      TokenType2.EQUAL -> visitor.text(xmlTokenizer.text)
      TokenType2.COMMENT -> visitor.comment(xmlTokenizer.text.removePrefix("<!--").removeSuffix("-->"))
      TokenType2.CDATA -> visitor.cdata(trimCdata(xmlTokenizer.text))
      TokenType2.SLASH -> visitor.text(xmlTokenizer.text)
      TokenType2.CONFIG_START -> readConfig(xmlTokenizer = xmlTokenizer, visitor = visitor)
      TokenType2.CONFIG_END -> TODO()
    }
  }

  private fun readConfig(xmlTokenizer: XmlTokenizer, visitor: XmlVisitor) {
    check(xmlTokenizer.next())
    check(xmlTokenizer.type == TokenType2.SYMBOL)
    visitor.startConfig(xmlTokenizer.text)

    while (xmlTokenizer.next()) {
      when (xmlTokenizer.type) {
        TokenType2.SYMBOL -> {
          val attrName = xmlTokenizer.text
          check(xmlTokenizer.next())
          check(xmlTokenizer.type == TokenType2.EQUAL)
          check(xmlTokenizer.next())
          check(xmlTokenizer.type == TokenType2.STRING)
          val attrValue = xmlTokenizer.text.removePrefix("\"").removeSuffix("\"")
          visitor.attribute(name = attrName, value = attrValue)
        }

        TokenType2.WHITESPACE -> visitor.text(xmlTokenizer.text)
        TokenType2.CONFIG_END -> {
          visitor.endConfig()
          break
        }

        else -> TODO()
      }
    }
  }

  private fun readTag(xmlTokenizer: XmlTokenizer, visitor: XmlVisitor) {
    check(xmlTokenizer.next())
//    check() {"Illegal token type: ${xmlTokenizer.type}: \"${xmlTokenizer.text}\""}
    when (xmlTokenizer.type) {
      TokenType2.SYMBOL -> {
        val tagName = xmlTokenizer.text
        visitor.startOpenTag(tagName)
        while (xmlTokenizer.next()) {
          when (xmlTokenizer.type) {
            TokenType2.TAG_START -> TODO()
            TokenType2.TAG_END -> {
              visitor.endOpenTag(false)
              break
            }

            TokenType2.SYMBOL -> {
              val attrName = xmlTokenizer.text
              check(xmlTokenizer.next())
              check(xmlTokenizer.type == TokenType2.EQUAL)
              check(xmlTokenizer.next())
              check(xmlTokenizer.type == TokenType2.STRING){"Invalid token type: ${xmlTokenizer.type}. Text: ${xmlTokenizer.text}"}
              val attrValue = xmlTokenizer.text.removePrefix("\"").removeSuffix("\"")
              visitor.attribute(name = attrName, value = attrValue)
            }

            TokenType2.WHITESPACE -> visitor.text(xmlTokenizer.text)
            TokenType2.STRING -> TODO()
            TokenType2.TEXT -> TODO()
            TokenType2.EQUAL -> TODO()
            TokenType2.COMMENT -> TODO()
            TokenType2.CDATA -> TODO()
            TokenType2.SLASH -> {
              check(xmlTokenizer.next())
              check(xmlTokenizer.type == TokenType2.TAG_END)
              visitor.endOpenTag(true)
              break
            }

            TokenType2.CONFIG_START -> TODO()
            TokenType2.CONFIG_END -> TODO()
          }
        }
      }

      TokenType2.TAG_START -> TODO()
      TokenType2.TAG_END -> TODO()
      TokenType2.WHITESPACE -> TODO()
      TokenType2.STRING -> TODO()
      TokenType2.TEXT -> TODO()
      TokenType2.EQUAL -> TODO()
      TokenType2.COMMENT -> TODO()
      TokenType2.CDATA -> TODO()
      TokenType2.SLASH -> {
        check(xmlTokenizer.next())
        check(xmlTokenizer.type == TokenType2.SYMBOL)
        val endTagName = xmlTokenizer.text
        check(xmlTokenizer.next())
        check(xmlTokenizer.type == TokenType2.TAG_END)
        visitor.endTag(endTagName)
      }

      TokenType2.CONFIG_START -> TODO()
      TokenType2.CONFIG_END -> TODO()
    }

  }
}
