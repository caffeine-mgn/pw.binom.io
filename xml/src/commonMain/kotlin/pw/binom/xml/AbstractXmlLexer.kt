package pw.binom.xml

import pw.binom.xml.sax.XmlConsts

open class AbstractXmlLexer {
  var column = 0
  var line = 0
  var position = 0
  var isEof = false
  var tokenType = TokenType.UNKNOWN

  @PublishedApi
  internal var lastChar: Char? = null

  @PublishedApi
  internal val sb = StringBuilder()
  var text = ""

  val Char.isEmpty
    get() = this == '\n' || this == ' ' || this == '\r' || this == '\t'

  @PublishedApi
  internal inline fun commonReadString(firstChar: Char, reader: () -> Char?): String {
    column++
    position++
    sb.append(firstChar)
    tokenType = TokenType.STRING
    while (true) {
      val c = reader()
      if (c == null) {
        isEof = true
        lastChar = null
        throw UnexpectedEOFException()
      }
      when (c) {
        firstChar -> {
          column++
          position++
          lastChar = null
          sb.append(firstChar)
          val char = sb.toString()
          sb.clear()
          return char
        }

        else -> {
          position++
          if (c == '\n') {
            line++
            column = 0
          } else {
            column++
          }
          lastChar = null
          sb.append(c)
        }
      }
    }
  }

  @PublishedApi
  internal inline fun commonReadEmptySpace(startChar: Char, reader: () -> Char?): String {
    sb.clear()
    position++
    if (startChar == '\n') {
      line++
      column = 0
    } else {
      column++
    }
    sb.append(startChar)

    while (true) {
      val c = reader()
      when {
        c != null && c.isEmpty -> {
          position++
          if (c == '\n') {
            line++
            column = 0
          } else {
            column++
          }
          sb.append(c)
        }

        else -> {
          lastChar = c
          if (c == null) {
            isEof = true
          }
          val str = sb.toString()
          sb.clear()
          tokenType = TokenType.EMPTY
          return str
        }
      }
    }
  }

  @PublishedApi
  internal inline fun commonReadSymbol(char: Char, reader: () -> Char?): Boolean {
    column++
    position++
    sb.clear()
    sb.append(char)
    tokenType = TokenType.SYMBOL
    while (true) {
      val c = reader()
      if (c == null) {
        isEof = true
        text = sb.toString()
        sb.clear()
        return true
      }
      when (c) {
        '[', ']', '!', XmlConsts.TAG_START, XmlConsts.TAG_END, '/', '=', '"', '?', '&', ';', '\n', ' ', '\r', '\t' -> {
          lastChar = c
          text = sb.toString()
          sb.clear()
          return true
        }

        else -> {
          column++
          position++
          sb.append(c)
        }
      }
    }
  }

  inline fun commonNext(reader: () -> Char?): Boolean {
    if (isEof) {
      return false
    }
    val char = lastChar ?: reader()
    if (char == null) {
      isEof = true
      return false
    }
    lastChar = null
    when {
      char == XmlConsts.TAG_START -> {
        position++
        column++
        tokenType = TokenType.TAG_START
        text = "<"
        return true
      }

      char == XmlConsts.TAG_END -> {
        position++
        column++
        tokenType = TokenType.TAG_END
        text = ">"
        return true
      }

      char == '/' -> {
        position++
        column++
        tokenType = TokenType.SLASH
        text = "/"
        return true
      }

      char == '!' -> {
        position++
        column++
        text = "!"
        tokenType = TokenType.EXCLAMATION
        return true
      }

      char == '[' -> {
        position++
        column++
        text = "["
        tokenType = TokenType.LEFT_BRACKET
        return true
      }

      char == ']' -> {
        position++
        column++
        text = "]"
        tokenType = TokenType.RIGHT_BRACKET
        return true
      }

      char == '=' -> {
        position++
        column++
        text = "="
        tokenType = TokenType.EQUALS
        return true
      }

      char == '&' -> {
        position++
        column++
        tokenType = TokenType.AMPERSAND
        text = "&"
        return true
      }

      char == '-' -> {
        position++
        column++
        tokenType = TokenType.MINUS
        text = "-"
        return true
      }

      char == ';' -> {
        position++
        column++
        tokenType = TokenType.SEMICOLON
        text = ";"
        return true
      }

      char == '"' || char == '\'' -> {
        text = commonReadString(firstChar = char, reader = reader)
        return true
      }

      char == '?' -> {
        position++
        column++
        tokenType = TokenType.QUESTION
        text = "?"
        return true
      }

      char.isEmpty -> {
        text = commonReadEmptySpace(startChar = char, reader = reader)
        return true
      }

      else -> {
        return commonReadSymbol(char = char, reader = reader)
      }
    }
  }

  inline fun nextSkipEmpty(reader: () -> Char?): Boolean {
    while (commonNext(reader)) {
      if (tokenType != TokenType.EMPTY) {
        return true
      }
    }
    return false
  }
}
