package pw.binom.xml.sax

import pw.binom.collections.Stack
import pw.binom.io.Reader
import pw.binom.xml.AbstractXmlLexer
import pw.binom.xml.TokenType

class SyncXmlReaderVisitor(val lexer:AbstractXmlLexer,val reader: Reader) {
  private class Record(val name: String, val visitor: SyncXmlVisitor)
  private val visitors = Stack<Record>().asLiFoQueue()
  private fun next()=lexer.commonNext { reader.read() }
  private fun nextSkipEmpty()=lexer.nextSkipEmpty { reader.read() }

  /**
   * Чтение начала с <
   */
  private fun t1() {
    if (!next()) {
      TODO()
    }
    when (lexer.tokenType) {
      TokenType.SYMBOL -> t2()
      TokenType.SLASH -> closeTag()
      TokenType.EXCLAMATION -> readCDATA()

      else -> TODO("->${lexer.text}")
    }
  }

  /**
   * Чтение CDATA. Прочитанно "<!"
   */
  private fun readCDATA() {
    if (!next()) {
      TODO()
    }
    if (lexer.tokenType != TokenType.LEFT_BRACKET) {
      TODO()
    }
    if (!next()) {
      TODO()
    }
    if (lexer.tokenType != TokenType.SYMBOL) {
      TODO()
    }
    if (lexer.text != "CDATA") {
      TODO()
    }
    if (!next()) {
      TODO()
    }
    if (lexer.tokenType != TokenType.LEFT_BRACKET) {
      TODO()
    }
    val v = visitors.peek()
    val data = StringBuilder()
    while (true) {
      if (!next()) {
        TODO()
      }
      if (lexer.tokenType == TokenType.RIGHT_BRACKET) {
        if (!next()) {
          TODO()
        }
        if (lexer.tokenType != TokenType.RIGHT_BRACKET) {
          TODO()
        }
        if (!next()) {
          TODO()
        }
        if (lexer.tokenType != TokenType.TAG_END) {
          TODO()
        }
        v.visitor.cdata(data.toString())
        break
      }

      data.append(lexer.text)
    }
  }

  /**
   * Закрытие. "</" уже прочитано
   */
  private fun closeTag() {
    if (!next()) {
      TODO()
    }
    if (lexer.tokenType != TokenType.SYMBOL) {
      TODO()
    }
    val visitor = visitors.peek()
    val tagName = lexer.text
    if (visitor.name != tagName) {
      throw ExpectedException("Expected closing of tag [${visitor.name}] but got [$tagName]")
    }
    if (!next()) {
      TODO()
    }
    if (lexer.tokenType != TokenType.TAG_END) {
      TODO()
    }
    visitor.visitor.end()
    visitors.pop()
  }

  /**
   * Чтение тега. После < идёт какой-то текст
   */
  private fun t2() {
    val nodeName = lexer.text
    val subNode = visitors.peek().visitor.subNode(nodeName)
    visitors.push(Record(nodeName, subNode))
    subNode.start(nodeName)
    if (!nextSkipEmpty()) {
      TODO()
    }

    fun readAttribute() {
      val attribute = lexer.text
      if (!nextSkipEmpty()) {
        TODO()
      }
      if (lexer.tokenType != TokenType.EQUALS) {
        TODO("Обработка аттрибута без значения")
      }
      if (!nextSkipEmpty()) {
        TODO()
      }
      if (lexer.tokenType != TokenType.STRING) {
        TODO()
      }
      val value = lexer.text.removePrefix("\"").removeSuffix("\"")
      subNode.attribute(
        name = attribute,
        value = value,
      )
    }

    when (lexer.tokenType) {
      TokenType.SYMBOL -> {
        while (true) {
          readAttribute()
          if (!nextSkipEmpty()) {
            TODO()
          }
          when (lexer.tokenType) {
            TokenType.SYMBOL -> continue
            TokenType.TAG_END -> break
            TokenType.SLASH -> {
              if (!nextSkipEmpty()) {
                TODO()
              }
              if (lexer.tokenType != TokenType.TAG_END) {
                TODO()
              }
              subNode.end()
              visitors.pop()
              break
            }

            else -> TODO()
          }
        }
      }

      TokenType.SLASH -> {
        if (!nextSkipEmpty()) {
          TODO()
        }
        if (lexer.tokenType != TokenType.TAG_END) {
          TODO()
        }
        subNode.end()
        visitors.pop()
      }

      TokenType.TAG_END -> {
        accept()
      }

      else -> TODO()
    }
  }

  private fun accept() {
    var t = false
    while (next()) {
      when (lexer.tokenType) {
        TokenType.TAG_START -> {
          t1()
        }

        else -> {
          if (lexer.text.isBlank() && !t) {
            continue
          }
          t = true
          visitors.peek().visitor.value(lexer.text)
        }
      }
    }
  }

  fun accept(visitor: SyncXmlVisitor) {
    visitors.push(Record("ROOT", visitor))
    do {
      accept()
    } while (visitors.size > 1)
  }
}
